package com.ning.atlas.aws;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DisableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.EnableAvailabilityZonesForLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

public class ELBInstaller extends ConcurrentComponent
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
        String member_query = uri.getParams().get("member_query");

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());
        Set<String> instance_ids = d.getSpace().query(member_query + ":ec2-instance-id");

        AmazonEC2Client client = new AmazonEC2Client(creds.toAWSCredentials());
        DescribeInstancesRequest dir = new DescribeInstancesRequest();
        dir.setInstanceIds(instance_ids);
        DescribeInstancesResult dires = client.describeInstances(dir);


        List<String> old_avail_zones = elb.describeLoadBalancers(new DescribeLoadBalancersRequest(asList(elb_name)))
                                              .getLoadBalancerDescriptions()
                                              .get(0)
                                              .getAvailabilityZones();

        List<String> new_avail_zones = Lists.newArrayList();
        for (Reservation reservation : dires.getReservations()) {
            for (com.amazonaws.services.ec2.model.Instance instance : reservation.getInstances()) {
                new_avail_zones.add(instance.getPlacement().getAvailabilityZone());
            }
        }

        List<Instance> instances = Lists.transform(Lists.newArrayList(instance_ids), new Function<String, Instance>()
        {
            @Override
            public Instance apply(String input)
            {
                return new Instance(input);
            }
        });

        RegisterInstancesWithLoadBalancerRequest rq = new RegisterInstancesWithLoadBalancerRequest(elb_name, instances);
        elb.registerInstancesWithLoadBalancer(rq);

        elb.enableAvailabilityZonesForLoadBalancer(new EnableAvailabilityZonesForLoadBalancerRequest(elb_name, new_avail_zones));

        List<String> zones_to_remove = Lists.newArrayList(old_avail_zones);
        zones_to_remove.removeAll(new_avail_zones);

        elb.disableAvailabilityZonesForLoadBalancer(new DisableAvailabilityZonesForLoadBalancerRequest(elb_name, zones_to_remove));

        return "okay";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
