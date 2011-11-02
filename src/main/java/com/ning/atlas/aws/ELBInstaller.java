package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Host;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Future;

public class ELBInstaller extends BaseComponent implements Installer
{
    private final static Logger logger = LoggerFactory.getLogger(EC2Provisioner.class);

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
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture(String.format("provision an elastic load balancer as %s", uri));
    }

    @Override
    public Future<?> install(Host server, Uri<Installer> uri, Deployment deployment)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
