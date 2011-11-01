package com.ning.atlas;

import com.ning.atlas.aws.EC2Helper;
import com.ning.atlas.galaxy.MicroGalaxyInstaller;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class TestScratch
{
    @Test
    @Ignore
    public void testAgainstAlreadySpunUp() throws Exception
    {
        Identity id = Identity.valueOf("/ning.0/gepo.0");
        Host h = new Host(id, "gepo", new My(), Collections.<Uri<Installer>>emptyList());
        Space space = InMemorySpace.newInstance();
        space.store(id, new Server("ec2-184-73-132-150.compute-1.amazonaws.com", "ip-10-116-55-77.ec2.internal"));

        Map<String, String> ssh_props = EC2Helper.loadSshPropertyThing("ugx_user", "xncore");

        MicroGalaxyInstaller mgi = new MicroGalaxyInstaller(ssh_props);
        Environment e = new Environment();
        Deployment d = new ActualDeployment(new SystemMap(Arrays.<Element>asList(h)), e, space);

        mgi.install(h, Uri.<Installer>valueOf("ugx:s3://atlas-ning/atlas-gepo-0.0.1-SNAPSHOT.tar.gz"), d).get();
    }
}
