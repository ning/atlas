package com.ning.atlas.aws;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.DisableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.EnableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
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

import javax.annotation.Nullable;
import javax.sql.rowset.serial.SerialStruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

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

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());


        String instance_id = d.getSpace().get(host.getId(), "ec2-instance-id")
                              .otherwise(new IllegalStateException(host.getId() + " lacks an ec2-instance-id"));
        Instance instance = new Instance(instance_id);
        RegisterInstancesWithLoadBalancerRequest rq = new RegisterInstancesWithLoadBalancerRequest(elb_name,
                                                                                                   asList(instance));
        elb.registerInstancesWithLoadBalancer(rq);

        return "okay";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        String elb_name = uri.getFragment();
        elbnames.add(elb_name);

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());

        String instance_id = d.getSpace().get(hostId, "ec2-instance-id")
                              .otherwise(new IllegalStateException(hostId + " lacks an ec2-instance-id"));

        DeregisterInstancesFromLoadBalancerRequest req = new DeregisterInstancesFromLoadBalancerRequest();
        req.setInstances(asList(new Instance(instance_id)));

        elb.deregisterInstancesFromLoadBalancer(req);

        // TODO figure out how to do this better
        return "okay";
    }

    @Override
    protected void finishLocal2(Deployment d)
    {
        /**
         * Clean up avail zones the b works against based on where instances are placed.
         */

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());
        AmazonEC2Client ec2 = new AmazonEC2Client(creds.toAWSCredentials());

        DescribeLoadBalancersResult rs = elb.describeLoadBalancers(new DescribeLoadBalancersRequest(Lists.newArrayList(elbnames)));

        for (LoadBalancerDescription description : rs.getLoadBalancerDescriptions()) {
            List<String> instance_ids = Lists.transform(description.getInstances(), new Function<Instance, String>()
            {
                @Override
                public String apply(Instance input)
                {
                    return input.getInstanceId();
                }
            });

            Set<String> new_zone_set = Sets.newHashSet();
            DescribeInstancesRequest des_instances_req = new DescribeInstancesRequest();
            des_instances_req.setInstanceIds(instance_ids);
            for (Reservation reservation : ec2.describeInstances(des_instances_req).getReservations()) {
                for (com.amazonaws.services.ec2.model.Instance instance : reservation.getInstances()) {
                    new_zone_set.add(instance.getPlacement().getAvailabilityZone());
                }
            }

            Set<String> old_zone_set = Sets.newHashSet(description.getAvailabilityZones());

            Set<String> to_remove = Sets.difference(old_zone_set, new_zone_set);
            Set<String> to_add = Sets.difference(new_zone_set, old_zone_set);

            if (!to_add.isEmpty()) {
                EnableAvailabilityZonesForLoadBalancerRequest eazreq = new EnableAvailabilityZonesForLoadBalancerRequest();
                eazreq.setAvailabilityZones(to_add);
                eazreq.setLoadBalancerName(description.getLoadBalancerName());
                elb.enableAvailabilityZonesForLoadBalancer(eazreq);
            }

            if (!to_remove.isEmpty()) {
                DisableAvailabilityZonesForLoadBalancerRequest disreq = new DisableAvailabilityZonesForLoadBalancerRequest();
                disreq.setAvailabilityZones(to_remove);
                disreq.setLoadBalancerName(description.getLoadBalancerName());
                elb.disableAvailabilityZonesForLoadBalancer(disreq);
            }

        }
    }
}
