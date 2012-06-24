package com.ning.atlas.components.aws;


import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.jclouds.compute.options.RunScriptOptions.Builder.runAsRoot;
import static org.jclouds.compute.predicates.NodePredicates.all;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.AWS.Credentials;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.space.Space;

/**
 *
 */
public class EC2Provisioner extends ConcurrentComponent
{
    private final static Logger logger = Logger.get(EC2Provisioner.class);

    private final ConcurrentMap<String, Boolean>   instanceState = Maps.newConcurrentMap();
    private final AtomicReference<ComputeService>    ec2           = new AtomicReference<ComputeService>();
    private final AtomicReference<String>          keypairId     = new AtomicReference<String>();
    private final String credentialName;

    public EC2Provisioner(Map<String, String> props)
    {
        this.credentialName = props.get("credentials");
    }


    public EC2Provisioner(AWSConfig config)
    {
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.getAccessKey());
        creds.setSecretKey(config.getSecretKey());

        ec2.set(AWS.computeCtx(creds).getComputeService());
        keypairId.set(config.getKeyPairId());
        this.credentialName = null;
    }

    @Override
    public String perform(final Host node, final Uri<? extends Component> uri, Deployment deployment) throws Exception
    {
        final Space space = deployment.getSpace();
        final Maybe<Server> s = space.get(node.getId(), Server.class, Missing.RequireAll);
        Maybe<EC2InstanceInfo> ec2info = space.get(node.getId(), EC2InstanceInfo.class, Missing.RequireAll);
        if (s.isKnown()
            && ec2info.isKnown()
            && instanceState.containsKey(ec2info.getValue().getEc2InstanceId())
            && instanceState.get(ec2info.getValue().getEc2InstanceId()))
        {
            // we have an ec2 instance for this node already
            logger.info("using existing ec2 instance %s for %s",
                        space.get(node.getId(), EC2InstanceInfo.class, Missing.RequireAll)
                             .getValue()
                             .getEc2InstanceId(),
                        node.getId().toExternalForm());

            return "using existing ec2 instance " + ec2info.getValue().getEc2InstanceId();
        }
        else {
            // spin up an ec2 instance for this node

            final ComputeService ec2 = EC2Provisioner.this.ec2.get();
            TemplateBuilder builder = ec2.templateBuilder(); 

            logger.info("Provisioning server for %s", node.getId());
            final String ami_name = uri.getFragment();
            builder.imageMatches(new Predicate<Image>(){

                @Override
                public boolean apply(Image input) {
                    // in jclouds imageId is currently namespaced w/ region
                    // providerId is not.
                    return input.getProviderId().equals(ami_name);
                }
                
            });
            
            if (uri.getParams().containsKey("instance_type")) {
                builder.hardwareId(uri.getParams().get("instance_type"));
            }
            
            Template template = builder.build();
            template.getOptions().as(EC2TemplateOptions.class).keyPair(keypairId.get());

            final String security_group = Maybe.elideNull(uri.getParams().get("security_group")).otherwise("default");
            AWS.waitForEC2SecurityGroup(security_group, deployment.getSpace(), 1, TimeUnit.MINUTES);
            template.getOptions().as(EC2TemplateOptions.class).securityGroups(security_group);
            
            String name = node.getId().toExternalForm().length() > 255
                    ? node.getId().toExternalForm().substring(0, 255)
                    : node.getId().toExternalForm();
            template.getOptions().userMetadata("Name", name);
            
            SSHCredentials creds = SSHCredentials.lookup(deployment.getSpace(), credentialName)
                                                 .otherwise(SSHCredentials.defaultCredentials(deployment.getSpace()))
                                                 .otherwise(new IllegalStateException("No SSH credentials available"));
            String privateKeyText = Files.toString(new File(creds.getKeyFilePath()), UTF_8);  
            
            NodeMetadata instance = null;
            try {
                instance = getOnlyElement(ec2.createNodesInGroup(name, 1, template));
                logger.debug("obtained ec2 instance %s", instance.getProviderId());
            } catch (RunNodesException e) {
                throw propagate(e);
            }

            template.getOptions().overrideLoginPrivateKey(privateKeyText);
            
            ExecResponse response = ec2.runScriptOnNode(instance.getId(), "ls -l", runAsRoot(false).wrapInInitScript(false).overrideLoginPrivateKey(privateKeyText));
            if (response.getExitStatus() == 0) {
                logger.info("Obtained instance %s at %s for %s",
                        instance.getProviderId(), instance.getPublicAddresses(), node.getId());
                Server server = new Server();
                server.setExternalAddress(get(instance.getPublicAddresses(), 0));
                server.setInternalAddress(get(instance.getPrivateAddresses(), 0));

                EC2InstanceInfo info = new EC2InstanceInfo();
                info.setEc2InstanceId(instance.getProviderId());
                space.store(node.getId(), info);
                space.store(node.getId(), server);
                return "created new ec2 instance " + info.getEc2InstanceId();
            } else {
                throw new RuntimeException("couldn't execute ls -l on instance: " + instance);
            }

        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        final EC2InstanceInfo ec2info = d.getSpace().get(hostId, EC2InstanceInfo.class, Missing.RequireAll)
                                   .otherwise(new IllegalStateException("Nop instance id found"));
        String instanceId = ec2info.getEc2InstanceId();
        destroyInstance(instanceId);
        
        return "terminated ec2 instance";
    }


    private void destroyInstance(final String instanceId)
    {
        final ComputeService ec2 = EC2Provisioner.this.ec2.get();
        ec2.destroyNodesMatching(new Predicate<NodeMetadata>(){

                @Override
                public boolean apply(NodeMetadata input) {
                    // in jclouds nodeId is currently namespaced w/ region
                    // providerId is not.
                    return input.getProviderId().equals(instanceId);
                }
                
            });
    }

    @Override
    protected void startLocal(Deployment deployment)
    {
        AtlasConfiguration config = AtlasConfiguration.global();

        Space s = deployment.getSpace();
        AWS.SSHKeyPairInfo info = s.get(AWS.ID, AWS.SSHKeyPairInfo.class, Missing.RequireAll)
                                   .otherwise(new IllegalStateException("unable to find aws ssh keypair info"));

        this.keypairId.set(info.getKeyPairId());
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());

        ec2.set(AWS.computeCtx(creds).getComputeService());
        
        Set<? extends NodeMetadata> instances = this.ec2.get().listNodesDetailsMatching(all());
        for (NodeMetadata instance : instances) {
            instanceState.put(instance.getProviderId(), instance.getStatus() == NodeMetadata.Status.RUNNING);
        }

    }


    @Override
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture("provision ec2 instance");
    }

    public void destroy(Identity id, Space space)
    {
        EC2InstanceInfo info = space.get(id, EC2InstanceInfo.class, Missing.RequireAll).getValue();
        destroyInstance(info.getEc2InstanceId());
        logger.info("destroyed ec2 instance %s", info.getEc2InstanceId());
    }

    public static class EC2InstanceInfo
    {
        private String ec2InstanceId;

        public String getEc2InstanceId()
        {
            return ec2InstanceId;
        }

        public void setEc2InstanceId(String ec2InstanceId)
        {
            this.ec2InstanceId = ec2InstanceId;
        }
    }
}
