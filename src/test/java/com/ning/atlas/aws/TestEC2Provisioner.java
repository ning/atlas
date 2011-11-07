package com.ning.atlas.aws;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Element;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestEC2Provisioner
{
    private EC2Provisioner   ec2;
    private Space            space;
    private Host             node;
    private SystemMap        map;
    private Environment      environment;
    private ActualDeployment deployment;


    public static Matcher<String> isAvailable()
    {
        return new BaseMatcher<String>()
        {
            @Override
            public boolean matches(Object item)
            {
                return System.getProperties().containsKey("RUN_EC2_TESTS") && new File(".awscreds").exists() ;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("RUN_EC2_TESTS and .awscreds both need to be there to run ec2 tests");
            }
        };
    }

    @Before
    public void setUp() throws Exception
    {
//        assumeThat("ec2", isAvailable());

        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        AWSConfig config = f.build(AWSConfig.class);
        this.ec2 = new EC2Provisioner(config);
        this.space = InMemorySpace.newInstance();
        this.node = new Host(Identity.root().createChild("test", "a"),
                             "ubuntu",
                             new My(),
                             Collections.<Uri<Installer>>emptyList());

        this.map = new SystemMap(Arrays.<Element>asList(node));
        this.ec2.start(deployment);
        this.environment = new Environment();
        this.deployment = new ActualDeployment(map, environment, space);
    }

    @After
    public void tearDown() throws Exception
    {

        this.ec2.destroy(node.getId(), space);
        this.ec2.finish(deployment);
    }

    @Test
    @Ignore
    public void testProvision() throws Exception
    {
        assumeThat("ec2", isAvailable());

        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Status> f = ec2.provision(node, uri, deployment);
        Status s = f.get();

        assertThat(s, not(nullValue()));
    }

    @Test
    @Ignore
    public void testIdempotentProvision() throws Exception
    {
        assumeThat("ec2", isAvailable());

        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Status> f = ec2.provision(node, uri, deployment);
        f.get();

        EC2Provisioner.EC2InstanceInfo info = space.get(node.getId(),
                                                        EC2Provisioner.EC2InstanceInfo.class,
                                                        Missing.RequireAll).getValue();
        ec2.provision(node, uri, deployment).get();

        EC2Provisioner.EC2InstanceInfo info2 = space.get(node.getId(),
                                                         EC2Provisioner.EC2InstanceInfo.class,
                                                         Missing.RequireAll).getValue();

        assertThat(info2.getEc2InstanceId(), equalTo(info.getEc2InstanceId()));
    }
}
