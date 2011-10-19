package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;

public class EC2Provisioner extends BaseComponent implements Provisioner
{
    private final static Logger logger = Logger.get(EC2Provisioner.class);

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

//    public Server provision(Base base, Node node) throws UnableToProvisionServerException
//    {
//        logger.info("Provisioning server for %s", node.getId());
//        RunInstancesRequest req = new RunInstancesRequest(base.getAttributes().get("ami"), 1, 1);
//
//        if (base.getAttributes().containsKey("instance_type")) {
//            req.setInstanceType(base.getAttributes().get("instance_type"));
//        }
//
//        req.setKeyName(keypairId);
//        RunInstancesResult rs = ec2.runInstances(req);
//
//        final Instance i = rs.getReservation().getInstances().get(0);
//
//        logger.debug("obtained ec2 instance {}", i.getInstanceId());
//
//        while (true) {
//            DescribeInstancesRequest dreq = new DescribeInstancesRequest();
//            dreq.setInstanceIds(Lists.newArrayList(i.getInstanceId()));
//            DescribeInstancesResult res = null;
//            try {
//                res = ec2.describeInstances(dreq);
//            }
//            catch (AmazonServiceException e) {
//                sometimes amazon says the instance doesn't exist yet,
//                if (!e.getMessage().contains("does not exist")) {
//                    throw new UnableToProvisionServerException(node.getId(), node.getType(), node.getName(), node.getMy(), "EC2 says the server we asked for doesn't exist");
//                }
//            }
//            if (res != null) {
//                Instance i2 = res.getReservations().get(0).getInstances().get(0);
//
//                if ("running".equals(i2.getState().getName())) {
//                    logger.info("Obtained instance %s at %s for %s",
//                                i2.getInstanceId(), i2.getPublicDnsName(), node.getId());
//                    return new Server(i2.getPrivateDnsName(), i2.getPublicDnsName(),
//                                      ImmutableMap.<String, String>of("instanceId", i2.getInstanceId()));
//                }
//                else {
//                    try {
//                        Thread.sleep(1000);
//                    }
//                    catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        throw new UnableToProvisionServerException(node.getId(), node.getType(), node.getName(), node.getMy(), "Interrupted while trying to provision");
//                    }
//                }
//            }
//        }
//        throw new UnsupportedOperationException("Not Yet Implemented!");
//    }


    @Override
    public Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server,
                            Uri<Provisioner> uri,
                            Space space,
                            SystemMap map)
    {
        return Futures.immediateFuture("provision ec2 instance");
    }

//    public void destroy(Server server)
//    {
//        ec2.terminateInstances(new TerminateInstancesRequest(asList(server.getAttributes().get("instanceId"))));
//    }
}
