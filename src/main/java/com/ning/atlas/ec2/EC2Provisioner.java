package com.ning.atlas.ec2;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ning.atlas.Server;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.template.ServerSpec;
import com.ning.atlas.template.SystemAssignment;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;

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

    public Set<Server> provisionBareServers(SystemAssignment m) throws InterruptedException
    {
        final Set<Callable<Boolean>> waiting = Sets.newLinkedHashSet();
        final Set<Server> servers = Sets.newLinkedHashSet();
        for (final ServerSpec spec : m.getInstances()) {
            RunInstancesRequest req = new RunInstancesRequest(spec.getBase(), 1, 1);

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
        Preconditions.checkArgument(!Strings.isNullOrEmpty(command), "command may not be empty or null");

        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(s.getExternalIpAddress());

        PKCS8KeyFile keyfile = new PKCS8KeyFile();
        keyfile.init(config.getPrivateKeyFile());
        ssh.authPublickey(config.getSshUserName(), keyfile);

        Session session = ssh.startSession();
        Session.Command c = session.exec(command);
        try {
            return c.getOutputAsString();
        }
        finally {
            c.close();
            session.close();
            ssh.disconnect();
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
