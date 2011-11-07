package com.ning.atlas;

import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestScratchInstaller
{
    @Test
    public void testAtInValue() throws Exception
    {
        Identity id = Identity.root().createChild("hello", "0");
        Host host = new Host(id,
                             "base",
                             new My(),
                             Collections.<Uri<Installer>>emptyList());
        SystemMap map = new SystemMap(Arrays.<Element>asList(host));
        Environment environment = new Environment();
        Space space = InMemorySpace.newInstance();
        Deployment d = new ActualDeployment(map, environment, space);

        ScratchInstaller scratch = new ScratchInstaller();
        Status json = scratch.install(host, Uri.<Installer>valueOf("scratch:hello=@"), d).get();
        assertThat(space.get("hello").getValue(), equalTo(id.toExternalForm()));
    }


    @Test
    public void testAtInKey() throws Exception
    {
        Identity id = Identity.root().createChild("hello", "0");
        Host host = new Host(id,
                             "base",
                             new My(),
                             Collections.<Uri<Installer>>emptyList());
        SystemMap map = new SystemMap(Arrays.<Element>asList(host));
        Environment environment = new Environment();
        Space space = InMemorySpace.newInstance();
        Deployment d = new ActualDeployment(map, environment, space);

        ScratchInstaller scratch = new ScratchInstaller();
        Status json = scratch.install(host, Uri.<Installer>valueOf("scratch:@:hello=world"), d).get();
        assertThat(space.get(id.toExternalForm() + ":" + "hello").getValue(), equalTo("world"));
    }

    @Test
    public void testMultipleThings() throws Exception
    {
        Identity id = Identity.root().createChild("hello", "0");
        Host host = new Host(id,
                             "base",
                             new My(),
                             Collections.<Uri<Installer>>emptyList());
        SystemMap map = new SystemMap(Arrays.<Element>asList(host));
        Environment environment = new Environment();
        Space space = InMemorySpace.newInstance();
        Deployment d = new ActualDeployment(map, environment, space);

        ScratchInstaller scratch = new ScratchInstaller();
        Status status = scratch.install(host, Uri.<Installer>valueOf("scratch:@:hello=world;waffle=@"), d).get();
        assertThat(space.get(id.toExternalForm() + ":" + "hello").getValue(), equalTo("world"));
        assertThat(space.get("waffle").getValue(), equalTo(id.toExternalForm()));
    }
}


