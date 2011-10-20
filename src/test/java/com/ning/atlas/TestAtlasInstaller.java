package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestAtlasInstaller
{
//    private static final JRubyTemplateParser parser = new JRubyTemplateParser();
//    private static final ExecutorService exec   = MoreExecutors.sameThreadExecutor();
//
//    private AWSConfig      config;
//    private EC2Provisioner ec2;
//    private Properties     props;
//
//    @Before
//    public void setUp() throws Exception
//    {
//        assumeThat(new File(".awscreds"), exists());
//
//        props = new Properties();
//        props.load(new FileInputStream(".awscreds"));
//        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
//        config = f.build(AWSConfig.class);
//        this.ec2 = new EC2Provisioner(config);
//    }


    @Test
    public void testSerializationInAtlasInstaller() throws Exception
    {
        // the two props are required. Yea!
        AtlasInstaller ai = new AtlasInstaller(ImmutableMap.<String, String>of("ssh_user", "brianm",
                                                                               "ssh_key_file", "~/.ssh/id_rsa"));

        Host child1 = new Host(Identity.root().createChild("ning", "0").createChild("child", "0"),
                               "base",
                               new My(),
                               asList(Uri.<Installer>valueOf("galaxy:rslv")));

        Host child2 = new Host(Identity.root().createChild("ning", "0").createChild("child", "1"),
                               "base",
                               new My(ImmutableMap.<String, Object>of("galaxy", "console")),
                               asList(Uri.<Installer>valueOf("galaxy:proc")));

        Bunch root = new Bunch(Identity.root()
                                       .createChild("ning", "0"), new My(), Arrays.<Element>asList(child1, child2));

        final Environment environment = new Environment();
        SystemMap map = new SystemMap(Arrays.<Element>asList(root));

        final Space space = InMemorySpace.newInstance();
        space.store(child1.getId(), new Server("10.0.0.1"));
        space.store(child2.getId(), new Server("10.0.0.2"));

        ObjectMapper mapper = ai.makeMapper(space, environment);
        String json = ai.generateSystemMap(mapper, map);
        System.out.println(json);
    }

    @Test
    public void testOnEc2() throws Exception
    {
        // assumeThat(System.getProperty("RUN_EC2_TESTS"), not(nullValue()));
        assumeThat(new File(".awscreds"), exists());


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
        Deployment deployment = new ActualDeployment(map, environment, space);


        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Server> future = ec2.provision(node, uri, deployment);
        Server s = future.get();

        assertThat(s, not(nullValue()));

        AtlasInstaller ai = new AtlasInstaller(ImmutableMap.<String, String>of("ssh_user", props.getProperty("aws.ssh-user"),
                                                                               "ssh_key_file", props.getProperty("aws.key-file-path")));

        String node_info = ai.install(node, Uri.<Installer>valueOf("atlas"), deployment).get();
        System.out.println(node_info);
        System.out.println(s.getExternalAddress());

        ec2.destroy(node.getId(), space);
        ec2.finish(map, space);
        ai.finish(map, space);
    }

}
