package com.ning.atlas.galaxy;

import com.ning.atlas.Environment;
import com.ning.atlas.InstalledServerTemplate;
import com.ning.atlas.InstalledTemplate;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.Template;
import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.tree.Trees;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.equalTo;
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

    @Test
    public void testEndToEnd() throws Exception
    {
        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());

        ExecutorService exec = Executors.newCachedThreadPool();

        JRubyTemplateParser parser = new JRubyTemplateParser();
        Template root = parser.parseSystem(new File("src/test/ruby/test_micro_galaxy_installer.rb"));
        Environment env = parser.parseEnvironment(new File("src/test/ruby/test_micro_galaxy_installer.rb"));

        InstalledTemplate installed = root.normalize(env)
                                          .provision(exec).get()
                                          .initialize(exec).get()
                                          .install(exec).get();


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.writeValue(System.out, installed);


        List<InstalledServerTemplate> nodes = Trees.findInstancesOf(installed, InstalledServerTemplate.class);
        try {
            assertThat(nodes.size(), equalTo(1));
        }
        finally {
            for (InstalledServerTemplate node : nodes) {
                ec2.destroy(node.getServer());
            }
        }
    }
}
