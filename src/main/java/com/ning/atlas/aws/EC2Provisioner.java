package com.ning.atlas.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Arrays.asList;

public class EC2Provisioner implements Provisioner
{
    private final static Logger logger = LoggerFactory.getLogger(EC2Provisioner.class);
    private final AmazonEC2Client ec2;
    private final String          keypairId;

    public EC2Provisioner(Map<String, String> attributes)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(attributes.get("access_key"),
                                                                  attributes.get("secret_key"));
        keypairId = attributes.get("keypair_id");
        ec2 = new AmazonEC2AsyncClient(credentials);
    }

    public EC2Provisioner(AWSConfig config)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        ec2 = new AmazonEC2Client(credentials);
        keypairId = config.getKeyPairId();

    }

    @Override
    public Server provision(Base base)
    {
        logger.debug("provisioning server for base {}", base.getName());
        RunInstancesRequest req = new RunInstancesRequest(base.getAttributes().get("ami"), 1, 1);

        if (base.getAttributes().containsKey("instance_type")) {
            req.setInstanceType(base.getAttributes().get("instance_type"));
        }

        req.setKeyName(keypairId);
        RunInstancesResult rs = ec2.runInstances(req);

        final Instance i = rs.getReservation().getInstances().get(0);

        logger.debug("obtained ec2 instance {}", i.getInstanceId());

        while (true) {
            DescribeInstancesRequest dreq = new DescribeInstancesRequest();
            dreq.setInstanceIds(Lists.newArrayList(i.getInstanceId()));
            DescribeInstancesResult res = null;
            try {
                res = ec2.describeInstances(dreq);
            }
            catch (AmazonServiceException e) {
                // sometimes amazon says the instance doesn't exist yet,
                if (!e.getMessage().contains("does not exist")) {
                    throw e;
                }
            }
            if (res != null) {
                Instance i2 = res.getReservations().get(0).getInstances().get(0);

//                if (i2.getPrivateDnsName().startsWith("ip-")) {
//                    logger.info("obtained an instance with a {} internal dns name, throwing it away",
//                                i2.getPrivateDnsName());
//                    ec2.terminateInstances(new TerminateInstancesRequest(asList(i2.getInstanceId())));
//                    return provision(base);
//                }


                if ("running".equals(i2.getState().getName())) {
                    logger.debug("ec2 instance {} is running", i.getInstanceId());
                    return new Server(i2.getPrivateDnsName(), i2.getPublicDnsName(),
                                      ImmutableMap.<String, String>of("instanceId", i2.getInstanceId()));
                }
                else {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new Server(i.getPrivateIpAddress(), i.getPublicIpAddress(),
                                          ImmutableMap.<String, String>of("instanceId", i2.getInstanceId()));
                    }
                }
            }
        }
    }

    public void destroy(Server server)
    {
        ec2.terminateInstances(new TerminateInstancesRequest(asList(server.getAttributes().get("instanceId"))));
    }
}
