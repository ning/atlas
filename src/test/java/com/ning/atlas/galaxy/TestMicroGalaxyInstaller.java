package com.ning.atlas.galaxy;

import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Provisioner;
import org.junit.Before;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestMicroGalaxyInstaller
{

    private Properties     props;
    private AWSConfig      config;
    private EC2Provisioner ec2;

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

//    @Test
//    public void testEndToEnd() throws Exception
//    {
//        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());
//
//        ExecutorService exec = MoreExecutors.sameThreadExecutor();
//
//        JRubyTemplateParser parser = new JRubyTemplateParser();
//        Template root = parser.parseSystem(new File("src/test/ruby/test_micro_galaxy_installer.rb"));
//        Environment env = parser.parseEnvironment(new File("src/test/ruby/test_micro_galaxy_installer.rb"));
//
//        InstalledElement installed = root.normalize(env)
//                                         .provision(new ErrorCollector(), exec).get()
//                                         .initialize(new ErrorCollector(), exec).get()
//                                         .install(new ErrorCollector(), exec).get();
//
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
//        mapper.writeValue(System.out, installed);
//

//        List<InstalledServer> nodes = Trees.findInstancesOf(installed, InstalledServer.class);
//
//        assertThat(nodes.size(), equalTo(1));
//
//        for (InstalledServer node : nodes) {
//            try {
//            ec2.destroy(node.getServer());
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }
//        System.out.println("WOOOOOOOT");
//    }
}
