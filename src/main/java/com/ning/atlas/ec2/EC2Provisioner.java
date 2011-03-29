package com.ning.atlas.ec2;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.ning.atlas.Server;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.template.ServerSpec;
import com.ning.atlas.template.SystemManifest;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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

    public Set<Server> provisionBareServers(SystemManifest m) throws InterruptedException
    {
        final Set<Callable<Boolean>> waiting = Sets.newLinkedHashSet();
        final Set<Server> servers = Sets.newLinkedHashSet();
        for (final ServerSpec spec : m.getInstances()) {
            RunInstancesRequest req = new RunInstancesRequest(spec.getImage(), 1, 1);

            req.setKeyName(config.getKeyPairId());
            RunInstancesResult rs = ec2.runInstances(req);

            final Instance i = rs.getReservation().getInstances().get(0);

            waiting.add(new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    DescribeInstancesRequest req = new DescribeInstancesRequest();
                    req.setInstanceIds(Lists.newArrayList(i.getInstanceId()));
                    DescribeInstancesResult res = ec2.describeInstances(req);
                    Instance i2 = res.getReservations().get(0).getInstances().get(0);
                    if (!"running".equals(i2.getState().getName())) {
                        return false;
                    }

                    servers.add(new EC2Server(spec, i2));

                    return true;
                }
            });

            do {
                Thread.sleep(1000);
                Iterator<Callable<Boolean>> itty = waiting.iterator();

                while (itty.hasNext()) {
                    try {
                        if (itty.next().call()) {
                            itty.remove();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            while (!waiting.isEmpty());

        }

        return servers;
    }

    public void bootStrapServers(Set<Server> servers) throws Exception
    {
        for (Server server : servers) {
            String bootstrap = server.getBootStrap();
            if (!Strings.isNullOrEmpty(bootstrap)) {
                executeRemote(server, bootstrap);
            }
        }
    }

    public void destroy(Collection<Server> servers)
    {
        List<String> instance_ids = Lists.newArrayListWithCapacity(servers.size());

        for (Server server : servers) {
            EC2Server ec2s = EC2Server.class.cast(server);
            instance_ids.add(ec2s.getInstance().getInstanceId());
        }

        TerminateInstancesRequest tr = new TerminateInstancesRequest(instance_ids);
        ec2.terminateInstances(tr);
    }

    public String executeRemote(Server s, String command) throws Exception
    {
        EC2Server server = (EC2Server) s;

        final JSch jsch = new JSch();
        int tries = 30;

        Session session = null;
        while (session == null || !session.isConnected() || tries-- > 0) {
            try {
                session = jsch.getSession(config.getSshUserName(), server.getExternalIpAddress(), 22);

                byte[] priv_key_bytes = Files.toByteArray(config.getPrivateKeyFile());

                jsch.addIdentity(config.getSshUserName(), priv_key_bytes, null, new byte[0]);

                session.setUserInfo(new UserInfo()
                {

                    public String getPassphrase()
                    {
                        return "";
                    }

                    public String getPassword()
                    {
                        return "";
                    }

                    public boolean promptPassword(String s)
                    {
                        return false;
                    }

                    public boolean promptPassphrase(String s)
                    {
                        return true;
                    }

                    public boolean promptYesNo(String s)
                    {
                        return true;
                    }

                    public void showMessage(String s)
                    {
                        System.out.println(s);
                    }
                });

                session.connect(10000);
            }
            catch (JSchException e) {
                System.out.println(e.getMessage());
                Thread.sleep(1000);
                session.disconnect();
            }
        }

        if (tries == 0 || session == null) {
            throw new UnsupportedOperationException("Not Yet Implemented!");
        }

        final ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command.getBytes());
        channel.connect();
        InputStream out = channel.getInputStream();
        try {
            return new String(ByteStreams.toByteArray(out));
        }
        finally {
            out.close();
            channel.disconnect();
            session.disconnect();
        }
    }


    static class EC2Server implements Server
    {
        private final ServerSpec spec;
        private final Instance   instance;

        EC2Server(ServerSpec spec, Instance instance)
        {
            this.spec = spec;
            this.instance = instance;
        }

        ServerSpec getSpec()
        {
            return spec;
        }

        Instance getInstance()
        {
            return instance;
        }

        public String getInternalIpAddress()
        {
            return instance.getPrivateIpAddress();
        }

        public String getBootStrap()
        {
            return spec.getBootStrap();
        }

        public String getExternalIpAddress()
        {
            return instance.getPublicIpAddress();
        }
    }
}
