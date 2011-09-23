package com.ning.atlas.chef;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.Base;
import com.ning.atlas.BoundTemplate;
import com.ning.atlas.Environment;
import com.ning.atlas.Identity;
import com.ning.atlas.Initialization;
import com.ning.atlas.InitializedServer;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.My;
import com.ning.atlas.ProvisionedServer;
import com.ning.atlas.ProvisionedSystem;
import com.ning.atlas.ProvisionedElement;
import com.ning.atlas.Provisioner;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import com.ning.atlas.ServerTemplate;
import com.ning.atlas.Node;
import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.errors.ErrorCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class TestUbuntuChefSoloInstaller
{
    private static final ObjectMapper mapper = new ObjectMapper();

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
    public void testExplicitSpinUp() throws Exception
    {
        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());

        Environment env = new Environment("ec2");
        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");

        Installer initializer = new UbuntuChefSoloInstaller(attributes);

        final Base base = new Base("ubuntu", env, "ec2",
                                   Collections.<Initialization>emptyList(),
                                   ImmutableMap.<String, String>of("ami", "ami-add819c4"));
        Server s = ec2.provision(base, new Node()
        {
            @Override
            public String getType()
            {
                return "type";
            }

            @Override
            public String getName()
            {
                return "name";
            }

            @Override
            public My getMy()
            {
                return new My();
            }

            @Override
            public Collection<? extends Node> getChildren()
            {
                return Collections.emptyList();
            }

            @Override
            public Identity getId()
            {
                return Identity.root();
            }
        });

        try {
            initializer.install(s, "role[server]",
                                new ProvisionedSystem(Identity.root(), "root", "0", new My(),
                                                      Lists.<ProvisionedElement>newArrayList()),
                                new ProvisionedServer(Identity.root(), "woof", "meow", new My(), s, Collections.<String>emptyList(), base));


            SSH ssh = new SSH(new File(props.getProperty("aws.key-file-path")),
                              "ubuntu",
                              s.getExternalAddress());
            String out = ssh.exec("java -version");
            assertThat(out, containsString("Java(TM) SE Runtime Environment"));
        }
        finally {
            ec2.destroy(s);
        }
    }

    @Test
    public void testInitializationVariations() throws Exception
    {
        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");

        UbuntuChefSoloInstaller i = new UbuntuChefSoloInstaller(attributes);

        String json = i.createNodeJsonFor("{ \"run_list\": [ \"role[java-core]\" ] }");
        assertThat(mapper.readValue(json, UbuntuChefSoloInstaller.Node.class),
                   equalTo(new UbuntuChefSoloInstaller.Node("role[java-core]")));
    }

    @Test
    public void testInitializationVariations2() throws Exception
    {
        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/chefplay123/chef-solo.tar.gz");

        UbuntuChefSoloInstaller i = new UbuntuChefSoloInstaller(attributes);

        String json = i.createNodeJsonFor("role[java-core]");

        assertThat(mapper.readValue(json, UbuntuChefSoloInstaller.Node.class),
                   equalTo(new UbuntuChefSoloInstaller.Node("role[java-core]")));
    }

    @Test
    public void testInitializationVariations3() throws Exception
    {
        Map<String, String> attributes =
            ImmutableMap.of("ssh_user", "ubuntu",
                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");

        UbuntuChefSoloInstaller i = new UbuntuChefSoloInstaller(attributes);

        String json = i.createNodeJsonFor("role[java-core], recipe[emacs]");
        assertThat(mapper.readValue(json, UbuntuChefSoloInstaller.Node.class),
                   equalTo(new UbuntuChefSoloInstaller.Node("role[java-core]", "recipe[emacs]")));
    }

//    @Test
//    public void testWithEC2Provisioner() throws Exception
//    {
//        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());
//        Provisioner p = ec2;
//        Map<String, String> attributes =
//            ImmutableMap.of("ssh_user", "ubuntu",
//                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
//                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");
//        Environment env = new Environment("ec2",
//                                          ImmutableMap.<String, Provisioner>of("ec2", p),
//                                          ImmutableMap.<String, Installer>of("chef-solo", new UbuntuChefSoloInstaller(attributes)));
//
//
//        ServerTemplate st = new ServerTemplate("server");
//        st.setBase("java-core");
//
//        Base java_core = new Base("java-core", env, "ec2",
//                                  ImmutableList.<Initialization>of(Initialization.parseUriForm("atlas"),
//                                                                   Initialization.parseUriForm("chef-solo:role[server]")),
//                                  ImmutableMap.<String, String>of("ami", "ami-add819c4"));
//        env.addBase(java_core);
//
//        BoundTemplate bt = st.normalize(env);
//        ExecutorService ex = Executors.newCachedThreadPool();
//        List<ListenableFuture<ProvisionedServer>> pt = bt.provision(new ErrorCollector(), ex);
//        for (ListenableFuture<ProvisionedServer> future : pt) {
//            future.get();
//        }
//
//
//        InitializedTemplate it = pt.initialize(new ErrorCollector(), ex).get();
//        assertThat(it, instanceOf(InitializedServer.class));
//        InitializedServer ist = (InitializedServer) it;
//
//        Server s = ist.getServer();
//        SSH ssh = new SSH(new File(props.getProperty("aws.key-file-path")),
//                          "ubuntu",
//                          s.getExternalAddress());
//        String out = ssh.exec("java -version");
//        assertThat(out, containsString("Java(TM) SE Runtime Environment"));
//        ex.shutdown();
//        ec2.destroy(s);
//    }

//    @Test
//    public void testSystemMapMakesItUp() throws Exception
//    {
//        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());
//
//        Map<String, String> attributes =
//            ImmutableMap.of("ssh_user", "ubuntu",
//                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
//                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");
//        Environment env = new Environment("ec2", ImmutableMap.<String, Provisioner>of("ec2", ec2), ImmutableMap.<String, Installer>of("chef-solo", new UbuntuChefSoloInstaller(attributes)), null);
//
//        ServerTemplate st = new ServerTemplate("server");
//        st.setBase("java-core");
//
//        Base java_core = new Base("java-core", env, "ec2",
//                                  ImmutableList.<Initialization>of(Initialization.parseUriForm("chef-solo:{\"run_list\":[\"role[server]\"]}")),
//                                  ImmutableMap.<String, String>of("ami", "ami-e2af508b"));
//        env.addBase(java_core);
//
//        BoundTemplate bt = st.normalize(env);
//        ExecutorService ex = Executors.newCachedThreadPool();
//        ProvisionedElement pt = bt.provision(new ErrorCollector(),ex).get();
//        InitializedTemplate it = pt.initialize(new ErrorCollector(), ex).get();
//        assertThat(it, instanceOf(InitializedServer.class));
//        InitializedServer ist = (InitializedServer) it;
//
//        Server s = ist.getServer();
//        SSH ssh = new SSH(new File(props.getProperty("aws.key-file-path")),
//                          "ubuntu",
//                          s.getExternalAddress());
//        String out = ssh.exec("cat /etc/atlas/system_map.json");
//
//        assertThat(out, containsString("\"type\" : \"server\""));
//        assertThat(out, containsString("external_address"));
//        assertThat(out, containsString("internal_address"));
//
//        ex.shutdown();
//        ec2.destroy(s);
//    }


//    @Test
//    public void testEndToEndOnEC2() throws Exception
//    {
//        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());
//        Map<String, String> attributes =
//            ImmutableMap.of("ssh_user", "ubuntu",
//                            "ssh_key_file", new File(props.getProperty("aws.key-file-path")).getAbsolutePath(),
//                            "recipe_url", "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz");
//        Environment env = new Environment("ec2",
//                                          ImmutableMap.<String, Provisioner>of("ec2", ec2),
//                                          ImmutableMap.<String, Installer>of("chef-solo", new UbuntuChefSoloInstaller(attributes)),
//                                          null);
//
//        Base java_core = new Base("server", env, "ec2",
//                                  ImmutableList.<Initialization>of(Initialization.parseUriForm("chef-solo:role[server]")),
//                                  ImmutableMap.<String, String>of("ami", "ami-e2af508b"));
//        env.addBase(java_core);
//
//
//        env.addInitializer("chef-solo", new UbuntuChefSoloInstaller(attributes));
//
//
//        ServerTemplate st = new ServerTemplate("shell");
//        st.setBase("server");
//        st.setCardinality(asList("eshell"));
//
//        BoundTemplate bt = st.normalize(env);
//        ProvisionedElement pt = bt.provision(new ErrorCollector(),MoreExecutors.sameThreadExecutor()).get();
//        InitializedTemplate it = pt.initialize(new ErrorCollector(), MoreExecutors.sameThreadExecutor()).get();
//
//        InitializedServer ist = (InitializedServer) it;
//        ec2.destroy(ist.getServer());
//    }
//
}
