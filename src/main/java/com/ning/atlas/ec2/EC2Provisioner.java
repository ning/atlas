package com.ning.atlas.ec2;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;

import static java.util.Arrays.asList;

public class EC2Provisioner implements Provisioner
{
    private final AWSConfig            config;
    private final AmazonEC2AsyncClient ec2;

    public EC2Provisioner(AWSConfig config)
    {
        this.config = config;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        ec2 = new AmazonEC2AsyncClient(credentials);

    }

    @Override
    public Server provision(Base base)
    {
        RunInstancesRequest req = new RunInstancesRequest(base.getAttributes().get("ami"), 1, 1);

        req.setKeyName(config.getKeyPairId());
        RunInstancesResult rs = ec2.runInstances(req);

        final Instance i = rs.getReservation().getInstances().get(0);

        while (true) {
            DescribeInstancesRequest dreq = new DescribeInstancesRequest();
            dreq.setInstanceIds(Lists.newArrayList(i.getInstanceId()));
            DescribeInstancesResult res = ec2.describeInstances(dreq);
            Instance i2 = res.getReservations().get(0).getInstances().get(0);
            if ("running".equals(i2.getState().getName())) {
                return new EC2Server(base, i2);
            }
            else {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new EC2Server(base, i);
                }
            }
        }
    }

    public void destroy(Server server) {
        EC2Server ec2s = EC2Server.class.cast(server);
        TerminateInstancesRequest tr = new TerminateInstancesRequest(asList(ec2s.instance.getInstanceId()));
        ec2.terminateInstances(tr);
    }

    private final class EC2Server implements Server
    {

        private final Base base;
        private final Instance instance;

        EC2Server(Base base, Instance instance)
        {
            this.base = base;
            this.instance = instance;
        }

        @Override
        public String getExternalIpAddress()
        {
            return instance.getPublicIpAddress();
        }

        @Override
        public String getInternalIpAddress()
        {
            return instance.getPrivateIpAddress();
        }

        @Override
        public ListenableFuture<? extends Server> initialize()
        {
            return null;
        }
    }
}
