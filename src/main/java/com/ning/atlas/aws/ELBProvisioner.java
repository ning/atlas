package com.ning.atlas.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerNotFoundException;
import com.amazonaws.services.elasticloadbalancing.model.SourceSecurityGroup;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Space;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

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

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());
        AmazonEC2Client ec2 = new AmazonEC2Client(creds.toAWSCredentials());
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

    private void ensureGroupsAllowed(Space space, AmazonElasticLoadBalancingClient elb, AmazonEC2Client ec2, String protocol, int to_port, String elb_name, Set<String> groups_to_allow) throws InterruptedException
    {
        for (String s : groups_to_allow) {
            AWS.waitForEC2SecurityGroup(s, space, 1, TimeUnit.MINUTES);
        }
        DescribeLoadBalancersRequest des_lb = new DescribeLoadBalancersRequest();
        des_lb.setLoadBalancerNames(asList(elb_name));
        SourceSecurityGroup source = elb.describeLoadBalancers(des_lb)
            .getLoadBalancerDescriptions()
            .get(0)
            .getSourceSecurityGroup();

        DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest();
        req.setGroupNames(groups_to_allow);

        DescribeSecurityGroupsResult res = ec2.describeSecurityGroups(req);
        Set<String> okay_groups = Sets.newHashSet();
        for (SecurityGroup group : res.getSecurityGroups()) {
            for (IpPermission permission : group.getIpPermissions()) {
                if (permission.getFromPort() == to_port && permission.getToPort() == to_port) {
                    for (UserIdGroupPair pair : permission.getUserIdGroupPairs()) {
                        if (source.getGroupName().equals(pair.getGroupName())) {
                            okay_groups.add(group.getGroupName());
                        }
                    }
                }
            }
        }

        Set<String> to_fix = Sets.difference(groups_to_allow, okay_groups);

        for (String group_name : to_fix) {
            AuthorizeSecurityGroupIngressRequest in_req = new AuthorizeSecurityGroupIngressRequest();
            in_req.setGroupName(group_name);
            in_req.setSourceSecurityGroupName(source.getGroupName());
            in_req.setSourceSecurityGroupOwnerId(source.getOwnerAlias());
            try {
                ec2.authorizeSecurityGroupIngress(in_req);
            }
            catch (AmazonServiceException e) {
                if ("InvalidPermission.Duplicate".equals(e.getErrorCode())) {
                    // it is okay, we are duping an existing. Our check for what to
                    // allow missed something. EC2 perms are a pain.
                }
                else {
                    throw e;
                }
            }
        }
    }

    private String ensureLbExists(AmazonElasticLoadBalancingClient aws,
                                  AmazonEC2Client ec2,
                                  String name,
                                  int from_port,
                                  int to_port,
                                  String protocol)
    {
        DescribeLoadBalancersRequest req = new DescribeLoadBalancersRequest(asList(name));
        try {
            DescribeLoadBalancersResult rs = aws.describeLoadBalancers(req);
            return rs.getLoadBalancerDescriptions().get(0).getDNSName();
        }
        catch (LoadBalancerNotFoundException e) {
            // not found!
            // we don't know what zones our instances are in yet, so add lb to all of them
            // we will rejigger this when we assign instances to the LB
            DescribeAvailabilityZonesResult avail_zones = ec2.describeAvailabilityZones();

            List<String> avail_zones_s = Lists.transform(avail_zones.getAvailabilityZones(),
                                                         new Function<AvailabilityZone, String>()
                                                         {
                                                             @Override
                                                             public String apply(AvailabilityZone input)
                                                             {
                                                                 return input.getZoneName();
                                                             }
                                                         });

            Listener listener = new Listener(protocol, from_port, to_port);
            CreateLoadBalancerRequest clbrq = new CreateLoadBalancerRequest(name, asList(listener), avail_zones_s);
            CreateLoadBalancerResult rs = aws.createLoadBalancer(clbrq);

            return rs.getDNSName();
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());

        elb.deleteLoadBalancer(new DeleteLoadBalancerRequest(uri.getFragment()));

        return "okay";
    }
}
