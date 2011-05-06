package com.ning.atlas.cruft;

public class EC2OldProvisioner
{
//    private final AWSConfig            config;
//    private final AmazonEC2AsyncClient cruft;
//
//    public EC2OldProvisioner(AWSConfig config)
//    {
//        this.config = config;
//        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
//        cruft = new AmazonEC2AsyncClient(credentials);
//    }
//
//    public Set<Server> provisionBareServers(NormalizedTemplate m) throws InterruptedException
//    {
//        final Set<Callable<Boolean>> waiting = Sets.newLinkedHashSet();
//        final Set<Server> servers = Sets.newLinkedHashSet();
//        for (final ServerSpec spec : m.getInstances()) {
//            RunInstancesRequest req = new RunInstancesRequest(spec.getBase(), 1, 1);
//
//            req.setKeyName(config.getKeyPairId());
//            RunInstancesResult rs = cruft.runInstances(req);
//
//            final Instance i = rs.getReservation().getInstances().get(0);
//
//            waiting.add(new Callable<Boolean>()
//            {
//                public Boolean call() throws Exception
//                {
//                    DescribeInstancesRequest req = new DescribeInstancesRequest();
//                    req.setInstanceIds(Lists.newArrayList(i.getInstanceId()));
//                    DescribeInstancesResult res = cruft.describeInstances(req);
//                    Instance i2 = res.getReservations().get(0).getInstances().get(0);
//                    if (!"running".equals(i2.getState().getName())) {
//                        return false;
//                    }
//
//                    servers.add(new EC2Server(spec, i2));
//
//                    return true;
//                }
//            });
//
//            do {
//                Thread.sleep(1000);
//                Iterator<Callable<Boolean>> itty = waiting.iterator();
//
//                while (itty.hasNext()) {
//                    try {
//                        if (itty.next().call()) {
//                            itty.remove();
//                        }
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            while (!waiting.isEmpty());
//
//        }
//
//        return servers;
//    }
//
//    public void destroy(Collection<Server> servers)
//    {
//        List<String> instance_ids = Lists.newArrayListWithCapacity(servers.size());
//
//        for (Server server : servers) {
//            EC2Server ec2s = EC2Server.class.cast(server);
//            instance_ids.add(ec2s.getInstance().getInstanceId());
//        }
//
//        TerminateInstancesRequest tr = new TerminateInstancesRequest(instance_ids);
//        cruft.terminateInstances(tr);
//    }
//
//    public String executeRemote(Server s, String command) throws Exception
//    {
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(command), "command may not be empty or null");
//
//        SSHClient ssh = new SSHClient();
//        ssh.addHostKeyVerifier(new PromiscuousVerifier());
//        ssh.connect(s.getExternalIpAddress());
//
//        PKCS8KeyFile keyfile = new PKCS8KeyFile();
//        keyfile.init(config.getPrivateKeyFile());
//        ssh.authPublickey(config.getSshUserName(), keyfile);
//
//        Session session = ssh.startSession();
//        Session.Command c = session.exec(command);
//        try {
//            return c.getOutputAsString();
//        }
//        finally {
//            c.close();
//            session.close();
//            ssh.disconnect();
//        }
//    }
//
//
//    static class EC2Server implements Server
//    {
//        private final ServerSpec spec;
//        private final Instance   instance;
//
//        EC2Server(ServerSpec spec, Instance instance)
//        {
//            this.spec = spec;
//            this.instance = instance;
//        }
//
//        ServerSpec getSpec()
//        {
//            return spec;
//        }
//
//        Instance getInstance()
//        {
//            return instance;
//        }
//
//        public String getInternalIpAddress()
//        {
//            return instance.getPrivateIpAddress();
//        }
//
//        public String getBootStrap()
//        {
//            return spec.getInit();
//        }
//
//        public String getName()
//        {
//            return spec.getName();
//        }
//
//        public Base getBase()
//        {
//            return new Base(spec.getBase());
//        }
//
//        public String getExternalIpAddress()
//        {
//            return instance.getPublicIpAddress();
//        }
//    }
}
