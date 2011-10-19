package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.Installer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Future;

public class ELBInstaller extends BaseComponent implements Installer
{
    private final static Logger logger = LoggerFactory.getLogger(EC2Provisioner.class);
    private final AmazonElasticLoadBalancingClient elb;

    public ELBInstaller(Map<String, String> attributes)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(attributes.get("access_key"),
                                                                  attributes.get("secret_key"));
        elb = new AmazonElasticLoadBalancingClient(credentials);
    }


//    @Override
//    public void install(Server s, String fragment, Node root, Node node)
//    {
//
//        Instance i = new Instance(s.getAttributes().get("instanceId"));
//        RegisterInstancesWithLoadBalancerRequest req = new RegisterInstancesWithLoadBalancerRequest(fragment,
//                                                                                                    asList(i));
//        elb.registerInstancesWithLoadBalancer(req);
//    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server,
                                   Uri<Installer> uri,
                                   Space space,
                                   SystemMap map)
    {
        return Futures.immediateFuture(String.format("provision an elastic load balancer as %s", uri));
    }

    @Override
    public Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
