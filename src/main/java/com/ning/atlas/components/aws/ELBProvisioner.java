package com.ning.atlas.components.aws;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jclouds.aws.ec2.AWSEC2Client;
import org.jclouds.ec2.domain.AvailabilityZoneInfo;
import org.jclouds.ec2.domain.IpPermission;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.ec2.util.IpPermissions;
import org.jclouds.elb.ELBApi;
import org.jclouds.elb.domain.Listener;
import org.jclouds.elb.domain.LoadBalancer;
import org.jclouds.elb.domain.Protocol;
import org.jclouds.elb.domain.SecurityGroupAndOwner;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.AWS.Credentials;
import com.ning.atlas.spi.space.Space;

public class ELBProvisioner extends ConcurrentComponent
{
    @Override
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture(String.format("provision an elastic load balancer as %s", uri));
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        String elb_name = uri.getFragment();
        int from_port = Integer.parseInt(uri.getParams().get("from_port"));
        int to_port = Integer.parseInt(uri.getParams().get("to_port"));
        String protocol = uri.getParams().get("protocol");

        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());

        ELBApi elb = AWS.elbApi(creds);
        AWSEC2Client ec2 = AWS.ec2Api(creds);
        String dns_name = ensureLbExists(elb, ec2, elb_name, from_port, to_port, protocol);

        Set<String> groups_to_allow = Sets.newHashSet();
        for (Map.Entry<String, String> entry : uri.getParams().entrySet()) {
            if (entry.getKey().startsWith("allow_group_")) {
                groups_to_allow.add(entry.getValue());
            }
        }
        ensureGroupsAllowed(d.getSpace(), elb, ec2, protocol, to_port, elb_name, groups_to_allow);

        d.getSpace().store(host.getId(), "external-address", dns_name);

        return "okay";
    }

    private void ensureGroupsAllowed(Space space, ELBApi elb, AWSEC2Client ec2, String protocol, int to_port, String elb_name, Set<String> groups_to_allow) throws InterruptedException
    {
        for (String s : groups_to_allow) {
            AWS.waitForEC2SecurityGroup(s, space, 1, TimeUnit.MINUTES);
        }
        
        Optional<SecurityGroupAndOwner> sourceOption = elb.getLoadBalancerApi().get(elb_name).getSourceSecurityGroup();
        checkState(sourceOption.isPresent(), "elb %s does not have a source security group configured", elb_name);
        SecurityGroupAndOwner source = sourceOption.get();

        // old jclouds security group syntax needs freshening
        Set<SecurityGroup> res = ec2.getSecurityGroupServices().describeSecurityGroupsInRegionById(null, Iterables.toArray(groups_to_allow, String.class));
        Set<String> okay_groups = Sets.newHashSet();
        for (SecurityGroup group : res) {
            for (IpPermission permission : group.getIpPermissions()) {
                if (permission.getFromPort() == to_port && permission.getToPort() == to_port) {
                    if (permission.getUserIdGroupPairs().values().contains(source.getName())) {
                        okay_groups.add(source.getName());
                    }
                }
            }
        }

        Set<String> to_fix = Sets.difference(groups_to_allow, okay_groups);

        for (String group_name : to_fix) {
            IpPermission ingress = IpPermissions.permitAnyProtocol().originatingFromUserAndSecurityGroup(source.getOwner(), source.getName());
            try {
                ec2.getSecurityGroupServices().authorizeSecurityGroupIngressInRegion(null, group_name, ingress);
            } catch (IllegalStateException e) {
                if (e.getMessage().indexOf("InvalidPermission.Duplicate") != -1) {
                    // it is okay, we are duping an existing. Our check for what to
                    // allow missed something. EC2 perms are a pain.
                } else {
                    throw e;
                }
            }
        }
    }

    private String ensureLbExists(ELBApi aws,
                                  AWSEC2Client ec2,
                                  String name,
                                  int from_port,
                                  int to_port,
                                  String protocol)
    {
        LoadBalancer lb = aws.getLoadBalancerApi().get(name);
        if (lb != null) {
            return lb.getDnsName();
        } else {
            // not found!
            // we don't know what zones our instances are in yet, so add lb to all of them
            // we will rejigger this when we assign instances to the LB
            Set<AvailabilityZoneInfo> avail_zones = ec2.getAvailabilityZoneAndRegionServices().describeAvailabilityZonesInRegion(null);

            Listener listener = Listener.builder().protocol(Protocol.valueOf(protocol)).port(from_port).instancePort(to_port).build();

            Iterable<String> avail_zones_s = Iterables.transform(avail_zones,
                                                         new Function<AvailabilityZoneInfo, String>()
                                                         {
                                                             @Override
                                                             public String apply(AvailabilityZoneInfo input)
                                                             {
                                                                 return input.getZone();
                                                             }
                                                         });

            return aws.getLoadBalancerApi().createLoadBalancerListeningInAvailabilityZones(name, listener, avail_zones_s);
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        ELBApi elb = AWS.elbApi(creds);

        elb.getLoadBalancerApi().delete(uri.getFragment());

        return "okay";
    }
}
