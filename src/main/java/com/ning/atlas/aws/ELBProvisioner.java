package com.ning.atlas.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerListenersRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
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
import com.ning.atlas.spi.space.Missing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

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
        String to = uri.getParams().get("to");
        int from_port = Integer.parseInt(uri.getParams().get("from_port"));
        int to_port = Integer.parseInt(uri.getParams().get("to_port"));
        String protocol = uri.getParams().get("protocol");

        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class).getValue();
        AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(creds.toAWSCredentials());
        AmazonEC2Client ec2 = new AmazonEC2Client(creds.toAWSCredentials());
        String dns_name = ensureLbExists(elb, ec2, elb_name, from_port, to_port, protocol);

        d.getSpace().store(host.getId(), "external-address", dns_name);

        return "okay";
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
        catch (AmazonClientException e) {
            // not found!
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
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    public static class ELB
    {

    }
}
