package com.ning.atlas.aws;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.space.Space;
import org.junit.Test;

import java.util.Collections;

public class TestELBProvisioner
{
    @Test
    public void testFoo() throws Exception
    {
        Space space = InMemorySpace.newInstance();
        AWS.Credentials creds = new AWS.Credentials();
        creds.setAccessKey("AKIAI5J3B4CICSA4QPBA");
        creds.setSecretKey("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");
        space.store(AWS.ID, creds);

        ELBProvisioner prov = new ELBProvisioner();

        Host host = new Host(Identity.valueOf("/server.test"),
                             Uri.<Provisioner>valueOf("elb:testing?port=80"),
                             Collections.<Uri<Installer>>emptyList(),
                             Collections.<Uri<Installer>>emptyList(),
                             new My());

        String out = prov.perform(host,
                                  Uri.<Component>valueOf("elb:testing?port=80"),
                                  new ActualDeployment(new SystemMap(host), new Environment(), space));
        System.out.println(out);
    }
}
