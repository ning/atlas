package com.ning.atlas.aws;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class ELBInstaller extends ConcurrentComponent
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
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
