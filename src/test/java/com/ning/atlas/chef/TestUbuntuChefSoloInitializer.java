package com.ning.atlas.chef;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ning.atlas.Base;
import com.ning.atlas.BoundTemplate;
import com.ning.atlas.Environment;
import com.ning.atlas.InitializedServerTemplate;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedServerTemplate;
import com.ning.atlas.ProvisionedSystemTemplate;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import com.ning.atlas.ServerTemplate;
import com.ning.atlas.ec2.AWSConfig;
import com.ning.atlas.ec2.EC2Provisioner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class TestUbuntuChefSoloInitializer
{
    private AWSConfig      config;
    private EC2Provisioner ec2;
    private Properties     props;

    @Before
    public void setUp() throws Exception
    {
        assumeThat(new File(".awscreds"), exists());

        props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        config = f.build(AWSConfig.class);
        this.ec2 = new EC2Provisioner(config);
    }

    @Test
    @Ignore("it is expensive to run test every time")
    public void testExplicitSpinUp() throws Exception
    {
        Environment env = new Environment("ec2");
        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.private-key-fle")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz");

        Initializer initializer = new UbuntuChefSoloInitializer(attributes);

        Server s = ec2.provision(new Base("ubuntu", env, ImmutableMap.<String, String>of("ami", "ami-e2af508b")));

        try {
            Server init_server = initializer.initialize(s, "{ \"run_list\": [ \"role[java-core]\" ] }",
                                                        new ProvisionedSystemTemplate("root", Lists.<ProvisionedTemplate>newArrayList()));


            SSH ssh = new SSH(new File(props.getProperty("aws.private-key-fle")),
                              "ubuntu",
                              init_server.getExternalIpAddress());
            String out = ssh.exec("java -version");
            assertThat(out, containsString("Java(TM) SE Runtime Environment"));
        }
        finally {
            ec2.destroy(s);
        }
    }

    @Test
    @Ignore("it is expensive to run test every time")
    public void testWithEC2Provisioner() throws Exception
    {
        Environment env = new Environment("ec2");
        env.setProvisioner(ec2);

        ServerTemplate st = new ServerTemplate("server");
        st.setBase("java-core");

        Base java_core = new Base("java-core", env, ImmutableMap.<String, String>of("ami", "ami-e2af508b"));
        java_core.addInit("chef-solo:{\"run_list\":[\"role[java-core]\"]}");
        env.addBase(java_core);

        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.private-key-fle")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz");
        env.addInitializer("chef-solo", new UbuntuChefSoloInitializer(attributes));


        BoundTemplate bt = st.normalize(env);
        ExecutorService ex = Executors.newCachedThreadPool();
        ProvisionedTemplate pt = bt.provision(ex).get();
        InitializedTemplate it = pt.initialize(ex, pt).get();
        assertThat(it, instanceOf(InitializedServerTemplate.class));
        InitializedServerTemplate ist = (InitializedServerTemplate) it;

        Server s = ist.getServer();
        SSH ssh = new SSH(new File(props.getProperty("aws.private-key-fle")),
                          "ubuntu",
                          s.getExternalIpAddress());
        String out = ssh.exec("java -version");
        assertThat(out, containsString("Java(TM) SE Runtime Environment"));
        ex.shutdown();
        ec2.destroy(s);
    }
}
