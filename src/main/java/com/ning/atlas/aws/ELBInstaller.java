package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.ning.atlas.Installer;
import com.ning.atlas.Server;
import com.ning.atlas.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Arrays.asList;

public class ELBInstaller implements Installer
{
    private final static Logger logger = LoggerFactory.getLogger(EC2Provisioner.class);
    private final AmazonElasticLoadBalancingClient elb;

    public ELBInstaller(Map<String, String> attributes)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(attributes.get("access_key"),
                                                                  attributes.get("secret_key"));
        elb = new AmazonElasticLoadBalancingClient(credentials);
    }


    @Override
    public void install(Server s, String fragment, Thing root, Thing node)
    {

        Instance i = new Instance(s.getAttributes().get("instanceId"));
        RegisterInstancesWithLoadBalancerRequest req = new RegisterInstancesWithLoadBalancerRequest(fragment,
                                                                                                    asList(i));
        elb.registerInstancesWithLoadBalancer(req);
    }
}
