package com.ning.atlas.spi.protocols;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBSecurityGroup;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsRequest;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Core;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.space.Space;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

public class AWS
{
    public static final Identity ID = Core.ID.createChild("aws", "config");


    private final AtomicBoolean initialized = new AtomicBoolean(false);

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
        AWSCredentials creds = space.get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                    .otherwise(new IllegalStateException("No AWS Credentials available"))
                                    .toAWSCredentials();
        initEC2GroupCache(creds);
        if (existingEc2Groups.contains(groupName)) {
            return;
        }

        AmazonEC2Client ec2 = new AmazonEC2Client(creds);

        long stop_at = unit.toMillis(time) + System.currentTimeMillis();
        while (System.currentTimeMillis() < stop_at) {
            DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest();
            req.setGroupNames(asList(groupName));
            try {
                ec2.describeSecurityGroups(req);
                existingEc2Groups.add(groupName);
                return;
            }
            catch (AmazonServiceException e) {
                if (e.getErrorCode().equals("InvalidGroup.NotFound")) {
                    // okay, doesn't exist yet, keep waiting
                    Thread.sleep(1000);
                }
                else {
                    throw e;
                }
            }
        }
        throw new InterruptedException("timed out waiting for security group " + groupName);
    }

    public static void waitForRDSSecurityGroup(String groupName,
                                               Space space,
                                               long time,
                                               TimeUnit unit) throws InterruptedException
    {
        AWSCredentials creds = space.get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                    .otherwise(new IllegalStateException("No AWS Credentials available"))
                                    .toAWSCredentials();
        initRDSGroupCache(creds);
        if (existingRdsGroups.contains(groupName)) {
            return;
        }

        AmazonRDSClient rds = new AmazonRDSClient(creds);

        long stop_at = unit.toMillis(time) + System.currentTimeMillis();
        while (System.currentTimeMillis() < stop_at) {
            DescribeDBSecurityGroupsRequest req = new DescribeDBSecurityGroupsRequest();
            req.setDBSecurityGroupName(groupName);
            try {
                rds.describeDBSecurityGroups(req);
                existingRdsGroups.add(groupName);
                return;
            }
            catch (AmazonServiceException e) {
                if (1+1 == 2) throw e;
                Thread.sleep(1000);
            }
        }
        throw new InterruptedException("timed out waiting for security group " + groupName);
    }

    private static synchronized void initEC2GroupCache(AWSCredentials creds)
    {
        AmazonEC2Client ec2 = new AmazonEC2Client(creds);

        for (SecurityGroup securityGroup : ec2.describeSecurityGroups().getSecurityGroups()) {
            existingEc2Groups.add(securityGroup.getGroupName());
        }
    }

    private static synchronized void initRDSGroupCache(AWSCredentials creds)
    {
        AmazonRDSClient rds = new AmazonRDSClient(creds);

        for (DBSecurityGroup group : rds.describeDBSecurityGroups().getDBSecurityGroups()) {
            existingRdsGroups.add(group.getDBSecurityGroupName());
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

        public AWSCredentials toAWSCredentials()
        {
            return new BasicAWSCredentials(this.getAccessKey(), this.getSecretKey());
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
}
