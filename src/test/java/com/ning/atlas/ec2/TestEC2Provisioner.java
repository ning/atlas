package com.ning.atlas.ec2;

import com.ning.atlas.Server;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.template.EnvironmentConfig;
import com.ning.atlas.template.Manifest;
import com.ning.atlas.template.ServerTemplate;
import com.ning.atlas.template.SystemTemplate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class TestEC2Provisioner
{
    private AWSConfig config;

    @Before
    public void setUp() throws Exception
    {
        assumeThat(new File(".awscreds"), exists());

        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        config = f.build(AWSConfig.class);
    }

    @Test
    public void testFoo() throws Exception
    {
        SystemTemplate root = new SystemTemplate("root");
        SystemTemplate cluster = new SystemTemplate("cluster");
        ServerTemplate server = new ServerTemplate("server");
        server.setImage("ami-a6f504cf");
        cluster.addChild(server, 2);
        root.addChild(cluster, 1);

        Manifest m = Manifest.build(new EnvironmentConfig(), root);

        Provisioner p = new EC2Provisioner(config);

        Set<Server> servers = p.provisionServers(m);

        try {
            assertThat(servers.size(), equalTo(2));
        }
        finally {
            p.destroy(servers);
        }
    }


    public static Matcher<File> exists()
    {
        return new BaseMatcher<File>()
        {
            public boolean matches(Object item)
            {
                File f = (File) item;
                return f.exists();
            }

            public void describeTo(Description description)
            {
                description.appendText("the expected file does not exist");
            }
        };
    }
}
