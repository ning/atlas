package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.asList;

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
    public void testFoo() throws Exception
    {


        Host child1 = new Host(Identity.root().createChild("ning", "0").createChild("child", "0"),
                               "base",
                               new My(),
                               asList(Uri.<Installer>valueOf("galaxy:rslv")));

        Host child2 = new Host(Identity.root().createChild("ning", "0").createChild("child", "1"),
                               "base",
                               new My(ImmutableMap.<String, Object>of("galaxy", "console")),
                               asList(Uri.<Installer>valueOf("galaxy:proc")));

        Bunch root = new Bunch(Identity.root().createChild("ning", "0") , new My(), Arrays.<Element>asList(child1, child2));

        final Environment e = new Environment();
        SystemMap map = new SystemMap(Arrays.<Element>asList(root));

        final Space space = InMemorySpace.newInstance();
        space.store(child1.getId(), new Server("10.0.0.1"));
        space.store(child2.getId(), new Server("10.0.0.2"));

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        SimpleModule module = new SimpleModule("waffles", new Version(1, 0, 0, ""));
        module.addSerializer(new JsonSerializer<Host>()
        {
            @Override
            public Class<Host> handledType()
            {
                return Host.class;
            }

            @Override
            public void serialize(Host value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
            {
                Maybe<Server> s = space.get(value.getId(), Server.class, Missing.RequireAll);
                if (s.isKnown()) {
                    jgen.writeObject(new ExtraHost(value, s.getValue(), e.getProperties()));
                }


            }
        });

        mapper.registerModule(module);

        String json = mapper.writeValueAsString(map.getSingleRoot());
        System.out.println(json);

    }

    public static class ExtraHost
    {

        private final Host   host;
        private final Server server;
        private final Map<String, String> environment;

        private static final ObjectMapper mapper = new ObjectMapper();

        @JsonAnyGetter
        public Map getProperties() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
        {
            Map map =  mapper.convertValue(host, Map.class);
            map.remove("children");
            return map;
        }

        public ExtraHost(Host host, Server server, Map<String, String> environment)
        {
            this.host = host;
            this.server = server;
            this.environment = environment;
        }

        public Map<String, String> getEnvironment()
        {
            return environment;
        }

        public Server getServer()
        {
            return server;
        }
    }

    /*
    @Test
    public void testExplicitSpinUp() throws Exception
    {
        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());

        Template t = parser.parseSystem(new File("src/test/ruby/test_atlas_initializer.rb"));
        Environment e = parser.parseEnvironment(new File("src/test/ruby/test_atlas_initializer.rb"));

        final ErrorCollector ec = new ErrorCollector();
        InitializedTemplate it = t.normalize(e).provision(ec, exec).get().initialize(ec,exec).get();

        List<InitializedServer> leaves = Trees.findInstancesOf(it, InitializedServer.class);
        assertThat(leaves.size(), equalTo(1));

        InitializedServer ist = leaves.get(0);
        SSH ssh = new SSH(new File(props.getProperty("aws.key-file-path")),
                          "ubuntu",
                          ist.getServer().getExternalAddress());

        String node_info = ssh.exec("cat /etc/atlas/node_info.json");
        assertThat(node_info, containsString("\"name\" : \"eshell\""));
        assertThat(node_info, containsString("\"instanceId\""));

        for (InitializedServer leave : leaves) {
            ec2.destroy(leave.getServer());
        }
    }
    */
}
