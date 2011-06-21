package com.ning.atlas;

import com.ning.atlas.ec2.AWSConfig;
import com.ning.atlas.ec2.EC2Provisioner;
import com.ning.atlas.tree.Trees;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;
import sun.tools.tree.FinallyStatement;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.equalTo;
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
    @Ignore("too expensive to run every time")
    public void testEndToEnd() throws Exception
    {
        ExecutorService exec = Executors.newCachedThreadPool();

        JRubyTemplateParser parser = new JRubyTemplateParser();
        Template root = parser.parseSystem(new File("src/test/ruby/test_micro_galaxy_installer.rb"));
        Environment env = parser.parseEnvironment(new File("src/test/ruby/test_micro_galaxy_installer.rb"));

        InstalledTemplate installed = root.normalize(env)
                                          .provision(exec).get()
                                          .initialize(exec).get()
                                          .install(exec).get();


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
