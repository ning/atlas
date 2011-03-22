package com.ning.atlas.ec2;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ning.atlas.Server;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.template.ServerSpec;
import com.ning.atlas.template.Manifest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EC2Provisioner implements Provisioner
{
    private final AWSConfig       config;
    private final AmazonEC2Client ec2;

    public EC2Provisioner(AWSConfig config)
    {
        this.config = config;
        BasicAWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        ec2 = new AmazonEC2Client(credentials);
    }

    public Set<Server> provisionServers(Manifest m)
    {
        Multimap<String, ServerSpec> by_type = ArrayListMultimap.create();
        for (ServerSpec instance : m.getInstances()) {
            by_type.put(instance.getImage(), instance);
        }

        Multimap<String, Instance> results = ArrayListMultimap.create();

        for (Map.Entry<String, Collection<ServerSpec>> entry : by_type.asMap().entrySet()) {
            String ami = entry.getKey();
            int count = entry.getValue().size();
            RunInstancesRequest req = new RunInstancesRequest(ami, count, count);
            req.setKeyName(config.getKeyPairId());
            RunInstancesResult res = ec2.runInstances(req);
            results.putAll(ami, res.getReservation().getInstances());
        }

        Set<Server> servers = Sets.newLinkedHashSet();

        for (ServerSpec spec : m.getInstances()) {
            Collection<Instance> instances = results.get(spec.getImage());
            Instance i = instances.iterator().next();
            results.remove(spec.getImage(), i);
            Server s = new EC2Server(spec, i);
            servers.add(s);
        }

        return servers;
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


    static class EC2Server implements Server
    {
        private final ServerSpec spec;
        private final Instance              instance;

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
    }
}
