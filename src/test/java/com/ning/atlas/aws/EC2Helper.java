package com.ning.atlas.aws;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Element;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.skife.config.ConfigurationObjectFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class EC2Helper
{
    public static Deployment spinUpSingleInstance() throws Exception
    {
        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        AWSConfig config = f.build(AWSConfig.class);
        EC2Provisioner ec2 = new EC2Provisioner(config);
        Space space = InMemorySpace.newInstance();
        Host node = new Host(Identity.root().createChild("test", "a"),
                             "ubuntu",
                             new My(),
                             Collections.<Uri<Installer>>emptyList());

        SystemMap map = new SystemMap(Arrays.<Element>asList(node));
        ec2.start(map, space);
        Environment environment = new Environment();
        ActualDeployment deployment = new ActualDeployment(map, environment, space);

        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Server> future = ec2.provision(node, uri, deployment);
        future.get();
        ec2.finish(map, space);
        return deployment;
    }

    public static Map<String, String> loadSshPropertyThing(String... extras) throws IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        Map<String, String> r = Maps.newHashMap();
        r.put("ssh_user", props.getProperty("aws.ssh-user"));
        r.put("ssh_key_file", props.getProperty("aws.key-file-path"));

        Iterator<String> esi = Arrays.asList(extras).iterator();
        while (esi.hasNext()) {
            r.put(esi.next(), esi.next());
        }

        return r;
    }

    public static void destroy(Deployment d) throws Exception
    {
        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        AWSConfig config = f.build(AWSConfig.class);
        EC2Provisioner ec2 = new EC2Provisioner(config);
        for (Host host : d.getSystemMap().findLeaves()) {
            ec2.destroy(host.getId(), d.getSpace());
        }
        ec2.finish(d.getSystemMap(), d.getSpace());
    }
}
