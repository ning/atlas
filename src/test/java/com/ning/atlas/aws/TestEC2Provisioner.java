package com.ning.atlas.aws;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.Element;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestEC2Provisioner
{
    private EC2Provisioner           ec2;
    private Space                    space;
    private Host node;
    private SystemMap                map;
    private Environment environment;
    private ActualDeployment deployment;

    @Before
    public void setUp() throws Exception
    {
        // assumeThat(System.getProperty("RUN_EC2_TESTS"), not(nullValue()));
        assumeThat(new File(".awscreds"), exists());


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
        this.ec2.start(map, space);
        this.environment = new Environment();
        this.deployment = new ActualDeployment(map, environment, space);
    }

    @After
    public void tearDown() throws Exception
    {
        this.ec2.destroy(node.getId(), space);
        this.ec2.finish(map, space);
    }

    @Test
    public void testProvision() throws Exception
    {
        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Server> f = ec2.provision(node, uri, deployment);
        Server s = f.get();

        assertThat(s, not(nullValue()));
    }

    @Test
    public void testIdempotentProvision() throws Exception
    {
        Uri<Provisioner> uri = Uri.valueOf("ec2:ami-a7f539ce");
        Future<Server> f = ec2.provision(node, uri, deployment);
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
