package com.ning.atlas.aws;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.Server;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestEC2Provisioner
{
    private EC2Provisioner ec2;
    private AWSConfig config;

    @Before
    public void setUp() throws Exception
    {
        assumeThat(new File(".awscreds"),  exists());

        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        ConfigurationObjectFactory f = new ConfigurationObjectFactory(props);
        config = f.build(AWSConfig.class);
        this.ec2 = new EC2Provisioner(config);
    }

    @Test
    @Ignore("it is expensive to run test every time")
    public void testFoo() throws Exception
    {
        Server s = ec2.provision(new Base("test-base",
                                          new Environment("test-env"),
                                          ImmutableMap.of("ami", "ami-a6f504cf")));
        assertThat(s, notNullValue());
        try {
            assertThat(s.getExternalIpAddress(), notNullValue());
            assertThat(s.getInternalIpAddress(), notNullValue());
        }
        finally {
            ec2.destroy(s);
        }
    }
}
