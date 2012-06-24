package com.ning.atlas.spi.protocols;

import static org.jclouds.concurrent.MoreExecutors.sameThreadExecutor;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jclouds.ContextBuilder;
import org.jclouds.aws.ec2.AWSEC2ApiMetadata;
import org.jclouds.aws.ec2.AWSEC2Client;
import org.jclouds.aws.ec2.AWSEC2ProviderMetadata;
import org.jclouds.aws.elb.AWSELBProviderMetadata;
import org.jclouds.aws.iam.AWSIAMProviderMetadata;
import org.jclouds.aws.rds.AWSRDSProviderMetadata;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.ec2.domain.SecurityGroup;
import org.jclouds.elb.ELBApi;
import org.jclouds.elb.ELBApiMetadata;
import org.jclouds.iam.IAMApi;
import org.jclouds.iam.IAMApiMetadata;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.rds.RDSApi;
import org.jclouds.rds.RDSApiMetadata;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Core;
import com.ning.atlas.spi.space.Space;

public class AWS
{
    public static final Identity ID = Core.ID.createChild("aws", "config");

    /**
     * Safe to cache forever as we don't delete and create in same process
     */
    private static final Set<String> existingEc2Groups = new ConcurrentSkipListSet<String>();
    private static final Set<String> existingRdsGroups = new ConcurrentSkipListSet<String>();

    public static void waitForEC2SecurityGroup(String groupName,
                                               Space space,
                                               long time,
                                               TimeUnit unit) throws InterruptedException
    {
        AtlasConfiguration config = AtlasConfiguration.global();
        
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        final AWSEC2Client ec2Api = AWS.ec2Api(creds);

        initEC2GroupCache(ec2Api);
        if (existingEc2Groups.contains(groupName)) {
            return;
        }

        Predicate<String> retryer = new RetryablePredicate<String>(new Predicate<String>(){

            @Override
            public boolean apply(String input) {
                return ec2Api.getSecurityGroupServices().describeSecurityGroupsInRegion(
                        null, input).size() == 1;
            }
            
        }, unit.toMillis(time));

        if (retryer.apply(groupName)) 
        {
            existingEc2Groups.add(groupName);
            return;
        }
        throw new InterruptedException("timed out waiting for security group " + groupName);
    }

    public static void waitForRDSSecurityGroup(String groupName,
                                               Space space,
                                               long time,
                                               TimeUnit unit) throws InterruptedException
    {
        AtlasConfiguration config = AtlasConfiguration.global();
        
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        final RDSApi rdsApi = AWS.rdsApi(creds);

        initRDSGroupCache(rdsApi);
        if (existingRdsGroups.contains(groupName)) {
            return;
        }
        
        Predicate<String> retryer = new RetryablePredicate<String>(new Predicate<String>(){

            @Override
            public boolean apply(String input) {
                return rdsApi.getSubnetGroupApi().get(input) != null;
            }
            
        }, unit.toMillis(time));

        if (retryer.apply(groupName)) 
        {
            existingRdsGroups.add(groupName);
            return;
        }
        throw new InterruptedException("timed out waiting for security group " + groupName);
    }

    private static synchronized void initEC2GroupCache(AWSEC2Client ec2)
    {

        for (SecurityGroup securityGroup : ec2.getSecurityGroupServices().describeSecurityGroupsInRegion(null)) {
            existingEc2Groups.add(securityGroup.getName());
        }
    }

    private static synchronized void initRDSGroupCache(RDSApi rds)
    {

        for (org.jclouds.rds.domain.SecurityGroup group : rds.getSecurityGroupApi().list().concat().toImmutableSet()) {
            existingRdsGroups.add(group.getName());
        }
    }

    public static class Credentials
    {
        private AtomicReference<String> accessKey = new AtomicReference<String>();
        private AtomicReference<String> secretKey = new AtomicReference<String>();

        public String getAccessKey()
        {
            return accessKey.get();
        }

        public void setAccessKey(String accessKey)
        {
            this.accessKey.set(accessKey);
        }

        public String getSecretKey()
        {
            return secretKey.get();
        }

        public void setSecretKey(String secretKey)
        {
            this.secretKey.set(secretKey);
        }
    }

    public static class SSHKeyPairInfo
    {
        private AtomicReference<String> name        = new AtomicReference<String>();
        private AtomicReference<String> keyPairFile = new AtomicReference<String>();

        public String getKeyPairId()
        {
            return name.get();
        }

        public void setKeyPairId(String name)
        {
            this.name.set(name);
        }

        public String getPrivateKeyFile()
        {
            return keyPairFile.get();
        }

        public void setPrivateKeyFile(String keyPairFile)
        {
            this.keyPairFile.set(keyPairFile);
        }
    }
    
    private final static Iterable<Module> JCLOUDS_MODULES = ImmutableSet.<Module> of(new ExecutorServiceModule(
            sameThreadExecutor(), sameThreadExecutor()));

    public static IAMApi iamApi(Credentials creds) {
        return ContextBuilder.newBuilder(new AWSIAMProviderMetadata())
                             .credentials(creds.getAccessKey(), creds.getSecretKey())
                             .modules(AWS.JCLOUDS_MODULES)
                             .build(IAMApiMetadata.CONTEXT_TOKEN).getApi();
    }
    
    public static ComputeServiceContext computeCtx(Credentials creds) {
        return ContextBuilder.newBuilder(new AWSEC2ProviderMetadata())
                             .credentials(creds.getAccessKey(), creds.getSecretKey())
                             .modules(AWS.JCLOUDS_MODULES)
                             .build(ComputeServiceContext.class);
    }
    
    public static AWSEC2Client ec2Api(Credentials creds) {
        return ContextBuilder.newBuilder(new AWSEC2ProviderMetadata())
                             .credentials(creds.getAccessKey(), creds.getSecretKey())
                             .modules(AWS.JCLOUDS_MODULES)
                             .build(AWSEC2ApiMetadata.CONTEXT_TOKEN).getApi();
    }
    
    public static ELBApi elbApi(Credentials creds) {
        return ContextBuilder.newBuilder(new AWSELBProviderMetadata())
                             .credentials(creds.getAccessKey(), creds.getSecretKey())
                             .modules(AWS.JCLOUDS_MODULES)
                             .build(ELBApiMetadata.CONTEXT_TOKEN).getApi();
    }
    
    public static RDSApi rdsApi(Credentials creds) {
        return ContextBuilder.newBuilder(new AWSRDSProviderMetadata())
                             .credentials(creds.getAccessKey(), creds.getSecretKey())
                             .modules(AWS.JCLOUDS_MODULES)
                             .build(RDSApiMetadata.CONTEXT_TOKEN).getApi();
    }
}
