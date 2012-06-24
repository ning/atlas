package com.ning.atlas.components.aws;

import static org.jclouds.elb.options.ListLoadBalancersOptions.Builder.names;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;

import org.jclouds.aws.ec2.AWSEC2Client;
import org.jclouds.aws.ec2.domain.AWSRunningInstance;
import org.jclouds.elb.ELBApi;
import org.jclouds.elb.domain.LoadBalancer;

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

public class ELBAddInstaller extends ConcurrentComponent
{

    private final Set<String> elbnames = new ConcurrentSkipListSet<String>();

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
        elbnames.add(elb_name);

        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        ELBApi elb = AWS.elbApi(creds);

        String instance_id = d.getSpace().get(host.getId(), "ec2-instance-id")
                              .otherwise(new IllegalStateException(host.getId() + " lacks an ec2-instance-id"));

        elb.getInstanceApi().registerInstanceWithLoadBalancer(instance_id, elb_name);

        return "okay";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        String elb_name = uri.getFragment();
        elbnames.add(elb_name);
        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        ELBApi elb = AWS.elbApi(creds);

        String instance_id = d.getSpace().get(hostId, "ec2-instance-id")
                              .otherwise(new IllegalStateException(hostId + " lacks an ec2-instance-id"));

        elb.getInstanceApi().deregisterInstanceFromLoadBalancer(instance_id, elb_name);

        // TODO figure out how to do this better
        return "okay";
    }

    @Override
    protected void finishLocal2(Deployment d)
    {
        /**
         * Clean up avail zones the b works against based on where instances are placed.
         */

        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        ELBApi elb = AWS.elbApi(creds);
        AWSEC2Client ec2 = AWS.ec2Api(creds);

        Iterable<LoadBalancer> lbs = elb.getLoadBalancerApi().list(names(elbnames));

        for (LoadBalancer lb : lbs) {
            Set<String> new_zone_set = Sets.newHashSet();
            for (AWSRunningInstance instance : Iterables.concat(ec2.getInstanceServices().describeInstancesInRegion(null, Iterables.toArray(lb.getInstanceIds(), String.class)))) {
                new_zone_set.add(instance.getAvailabilityZone());
            }

            Set<String> old_zone_set = Sets.newHashSet(lb.getAvailabilityZones());

            Set<String> to_remove = Sets.difference(old_zone_set, new_zone_set);
            Set<String> to_add = Sets.difference(new_zone_set, old_zone_set);

            if (!to_add.isEmpty()) {
                elb.getAvailabilityZoneApi().addAvailabilityZonesToLoadBalancer(to_add, lb.getName());
            }

            if (!to_remove.isEmpty()) {
                elb.getAvailabilityZoneApi().removeAvailabilityZonesFromLoadBalancer(to_remove, lb.getName());
            }
        }
    }
}
