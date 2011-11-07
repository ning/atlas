package com.ning.atlas.chef;

import com.google.common.collect.Iterables;
import com.ning.atlas.Host;
import com.ning.atlas.aws.EC2Helper;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import org.junit.Test;

import java.util.concurrent.Future;

import static com.ning.atlas.aws.EC2Helper.loadSshPropertyThing;
import static com.ning.atlas.aws.TestEC2Provisioner.isAvailable;
import static org.junit.Assume.assumeThat;

public class TestUbuntuChefSoloInstaller
{
    @Test
    public void testFoo() throws Exception
    {
        assumeThat("ec2", isAvailable());

        Deployment d = EC2Helper.spinUpSingleInstance();
        UbuntuChefSoloInstaller ci = new UbuntuChefSoloInstaller(loadSshPropertyThing("recipe_url",
                                                                                      "https://s3.amazonaws.com/atlas-resources/chef-solo.tar.gz"));
        Host h = Iterables.getOnlyElement(d.getSystemMap().findLeaves());
        Future<Status> f = ci.install(h, Uri.<Installer>valueOf("chef:role[server]"), d);
        System.out.println(f.get());
        EC2Helper.destroy(d);
        ci.finish(d);
    }
}
