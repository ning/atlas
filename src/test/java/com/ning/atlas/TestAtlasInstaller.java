package com.ning.atlas;

import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Provisioner;
import org.junit.Before;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestAtlasInstaller
{
    private static final JRubyTemplateParser parser = new JRubyTemplateParser();
    private static final ExecutorService exec   = MoreExecutors.sameThreadExecutor();

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
