package com.ning.atlas.galaxy;

import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Helper;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.spi.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestMicroGalaxyInstaller
{

    private Deployment d;
    private MicroGalaxyInstaller mgi;

    @Before
    public void setUp() throws Exception
    {
        this.d = EC2Helper.spinUpSingleInstance();
        this.mgi = new MicroGalaxyInstaller(EC2Helper.loadSshPropertyThing("ugx_user", "ubuntu"));
        mgi.start(d);
    }

    @After
    public void tearDown() throws Exception
    {
        EC2Helper.destroy(this.d);
        mgi.finish(d);
    }


    @Test
    public void testFoo() throws Exception
    {

    }
}
