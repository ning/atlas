package com.ning.atlas.badger;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.ServerTemplate;
import com.ning.atlas.SystemTemplate;
import com.ning.atlas.noop.NoOpInstaller;
import com.ning.atlas.noop.NoOpProvisioner;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class TestNewStyleStuff
{
    @Test
    public void testFoo() throws Exception
    {
        SystemTemplate root = new SystemTemplate("root", Collections.<String, Object>emptyMap());
        ServerTemplate child = new ServerTemplate("child", Collections.<String, Object>emptyMap());
        child.setBase("base");
        child.setInstall(Arrays.asList("foo:bar"));
        child.setCardinality(Arrays.asList("a", "b"));

        root.addChild(child);


        Environment env = new Environment("local",
                                          ImmutableMap.<Uri<Provisioner>, Provisioner>of(Uri.<Provisioner>valueOf("noop"), new NoOpProvisioner()),
                                          ImmutableMap.<Uri<Installer>, Installer>of(Uri.<Installer>valueOf("foo"), new NoOpInstaller()));

        env.addBase(new Base("base",
                             Uri.<Provisioner>valueOf("noop"),
                             Collections.<Uri<Installer>>emptyList(),
                             Collections.<String, String>emptyMap()));

        NormalizedTemplate norm = root.nom();

        DeploymentPlan dp = env.planDeploymentFor(norm, SystemMap.empty());

    }
}
