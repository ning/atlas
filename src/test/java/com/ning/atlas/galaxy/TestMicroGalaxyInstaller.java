package com.ning.atlas.galaxy;

import com.google.common.collect.Iterables;
import com.ning.atlas.AtlasInstaller;
import com.ning.atlas.Host;
import com.ning.atlas.aws.AWSConfig;
import com.ning.atlas.aws.EC2Helper;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.chef.UbuntuChefSoloInstaller;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.config.ConfigurationObjectFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.ning.atlas.aws.EC2Helper.loadSshPropertyThing;
import static com.ning.atlas.testing.AtlasMatchers.containsInstanceOf;
import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestMicroGalaxyInstaller
{

    private Deployment              d;
    private MicroGalaxyInstaller    mgi;
    private Host                    host;
    private UbuntuChefSoloInstaller chef;
    private AtlasInstaller atlas;

    @Before
    public void setUp() throws Exception
    {
        this.d = EC2Helper.spinUpSingleInstance();
        this.mgi = new MicroGalaxyInstaller(EC2Helper.loadSshPropertyThing("ugx_user", "ubuntu",
                                                                           "ugx_path", "/home/ubuntu/deploy/current"));
        mgi.start(d);
        this.host = Iterables.getOnlyElement(this.d.getSystemMap().findLeaves());

        this.atlas = new AtlasInstaller(loadSshPropertyThing());
        this.atlas.start(d);
        this.atlas.install(host, Uri.<Installer>valueOf("atlas"), d).get();
        chef = new UbuntuChefSoloInstaller(loadSshPropertyThing("recipe_url",
                                                                "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz"));
        chef.install(host, Uri.<Installer>valueOf("chef:role[ruby_server]"), d).get();
    }

    @After
    public void tearDown() throws Exception
    {
//        EC2Helper.destroy(this.d);
        mgi.finish(d);
        chef.finish(d);
        this.atlas.finish(d);
    }


    @Test
    public void testFoo() throws Exception
    {
        String uri = "mg:https://s3.amazonaws.com/atlas-resources/echo.tar.gz";
        String rs = this.mgi.install(host, Uri.<Installer>valueOf(uri), d).get();
        System.out.println(rs);
    }
}
