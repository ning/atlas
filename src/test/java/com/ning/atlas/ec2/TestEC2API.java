package com.ning.atlas.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.ning.atlas.ec2.AWSConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class TestEC2API
{
    @Test
    @Ignore
    public void testApp() throws IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        AWSConfig config = f.build(AWSConfig.class);

        AWSCredentials creds = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());

        AmazonEC2Client ec2 = new AmazonEC2Client(creds);

        int min_count = 1, max_count = 1;

        RunInstancesRequest req = new RunInstancesRequest("ami-f8b35e91", min_count, max_count);
        req.setKeyName(config.getKeyPairId());

        RunInstancesResult rs2 = ec2.runInstances(new RunInstancesRequest("ami-a6f504cf", min_count, max_count));


        for (Instance instance : rs2.getReservation().getInstances()) {
            System.out.println(instance.getInstanceId());
            ec2.terminateInstances(new TerminateInstancesRequest(Arrays.asList(instance.getInstanceId())));
        }
    }
}
