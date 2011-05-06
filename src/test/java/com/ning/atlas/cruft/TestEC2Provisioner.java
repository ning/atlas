package com.ning.atlas.cruft;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestEC2Provisioner
{

    @Test
    public void testwaffles() throws Exception
    {
        assertThat(1 + 1, equalTo(2));
    }

//    private AWSConfig config;
//    private Environment env;
//
//    @Before
//    public void setUp() throws Exception
//    {
//        assumeThat(new File(".awscreds"), exists());
//
//        Properties props = new Properties();
//        props.load(new FileInputStream(".awscreds"));
//        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
//        config = f.build(AWSConfig.class);
//        this.env = new Environment("cruft");
//    }
//
//    @Test
//    @Ignore
//    public void testALot() throws Exception
//    {
//        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("root");
//        ConfigurableSystemTemplate cluster = new ConfigurableSystemTemplate("cluster");
//        ConfigurableServerTemplate server = new ConfigurableServerTemplate("server");
//        server.setBase("ami-a6f504cf");
//        cluster.addChild(server, 2);
//        root.addChild(cluster, 1);
//
//        NormalizedTemplate m = NormalizedTemplate.build(new EnvironmentConfig(env), root);
//
//        OldProvisioner p = new EC2OldProvisioner(config);
//
//        Set<Server> servers = p.provisionBareServers(m);
//
//        try {
//            assertThat(servers.size(), equalTo(2));
//        }
//        finally {
//            p.destroy(servers);
//        }
//    }
//
//    @Test
//    @Ignore
//    public void testServersHaveInternalAddresses() throws Exception
//    {
//        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("root");
//
//        ConfigurableServerTemplate server = new ConfigurableServerTemplate("server");
//        server.setBase("ami-a6f504cf");
//        root.addChild(server, 1);
//
//        NormalizedTemplate m = NormalizedTemplate.build(new EnvironmentConfig(env), root);
//
//        OldProvisioner p = new EC2OldProvisioner(config);
//
//        Set<Server> servers = p.provisionBareServers(m);
//
//        try {
//            assertThat(servers.size(), equalTo(1));
//            Server s = servers.iterator().next();
//            assertThat(s.getInternalIpAddress(), notNullValue());
//        }
//        finally {
//            p.destroy(servers);
//        }
//    }
//
//    @Test
//    @Ignore
//    public void bootstrapChefServer() throws InterruptedException, IOException
//    {
//        JRubySystemTemplateParser parser = new JRubySystemTemplateParser();
//        DeployTemplate roots = parser.parse(new File("src/test/ruby/ex1/chef-server.rb"));
//
//        NormalizedTemplate m = NormalizedTemplate.build(new EnvironmentConfig(env), roots);
//
//        SSHBootStrapper bs = new SSHBootStrapper(config.getPrivateKeyFile(), config.getSshUserName());
//
//        OldProvisioner p = new EC2OldProvisioner(config);
//        Set<Server> s = p.provisionBareServers(m);
//        try {
//            Server chef_server = s.iterator().next();
//            bs.bootStrap(chef_server);
//            System.out.println(chef_server.getExternalIpAddress());
//
//            String out = bs.executeRemote(chef_server, "ps wwaux");
//            assertThat(out, containsString("rabbit"));
//            assertThat(out, containsString("couchdb"));
//            assertThat(out, containsString("solr"));
//            assertThat(out, containsString("chef-server"));
//
//        }
//        finally {
//            p.destroy(s);
//        }
//
//    }
//
//    @Test
//    @Ignore
//    public void testBootStrap() throws Exception
//    {
//        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("root");
//
//        ConfigurableServerTemplate server = new ConfigurableServerTemplate("server");
//        server.setInit("#!/bin/sh\nexport WAFFLE='hello world'\necho $WAFFLE > /tmp/booted\n");
//        server.setBase("ami-a6f504cf");
//        root.addChild(server, 1);
//
//        NormalizedTemplate m = NormalizedTemplate.build(new EnvironmentConfig(env), root);
//
//        OldProvisioner p = new EC2OldProvisioner(config);
//
//        Set<Server> servers = p.provisionBareServers(m);
//        Server s = servers.iterator().next();
//
//        SSHBootStrapper bs = new SSHBootStrapper(config.getPrivateKeyFile(), config.getSshUserName());
//
//        for (Server server1 : servers) {
//            bs.bootStrap(server1);
//        }
//
//        String out = bs.executeRemote(s, "cat /tmp/booted");
//        assertThat(out, containsString("hello world"));
//    }
//
//
//    public static Matcher<File> exists()
//    {
//        return new BaseMatcher<File>()
//        {
//            public boolean matches(Object item)
//            {
//                File f = (File) item;
//                return f.exists();
//            }
//
//            public void describeTo(Description description)
//            {
//                description.appendText("the expected file does not exist");
//            }
//        };
//    }
}
