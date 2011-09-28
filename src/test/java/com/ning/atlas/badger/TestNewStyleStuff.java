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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class TestNewStyleStuff
{
    private Environment env;
    private SystemMap map;
    private Deployment emptyDeployment;

    /**
     * TODO:
     * - Pass space into provisioners
     * - Pass space into installers
     * - handle the upgrade path
     * - initializations
     * - installations
     * - get rid of "single root hack" for passing system map to provisioners and installers (ie, a Node)
     * - make NormalizedTemplate *not* be a node (node needs to die die die)
     */

    @Before
    public void setUp() throws Exception
    {
        SystemTemplate root = new SystemTemplate("root", Collections.<String, Object>emptyMap());
        ServerTemplate child = new ServerTemplate("child", Collections.<String, Object>emptyMap());
        child.setBase("base");
        child.setInstall(Arrays.asList("foo:bar"));
        child.setCardinality(Arrays.asList("a", "b"));
        root.addChild(child);

        Map<Uri<Provisioner>, Provisioner> provisioners =
            ImmutableMap.<Uri<Provisioner>, Provisioner>of(Uri.<Provisioner>valueOf("noop"), new NoOpProvisioner());

        Map<Uri<Installer>, Installer> installers =
            ImmutableMap.<Uri<Installer>, Installer>of(Uri.<Installer>valueOf("foo"), new NoOpInstaller());

        env = new Environment("local",
                                          provisioners,
                                          installers);

        env.addBase(new Base("base",
                             Uri.<Provisioner>valueOf("noop"),
                             Collections.<Uri<Installer>>emptyList(),
                             Collections.<String, String>emptyMap()));

        map = root.normalize();

        emptyDeployment = Deployment.nil();
    }

    @Test
    public void testApiDesign() throws Exception
    {
        DeploymentPlan dp = env.planDeploymentFor(map, emptyDeployment);
        Deployment d = dp.deploy();
    }

    @Test
    public void testWatch() throws Exception
    {
        DeploymentPlan dp = env.planDeploymentFor(map, emptyDeployment);
        dp.watch(new DeploymentObserver()
        {

        });
    }

    @Test
    public void testDescribe() throws Exception
    {
        DeploymentPlan dp = env.planDeploymentFor(map, emptyDeployment);
        String externalized = dp.describe();
    }

    @Test
    public void testFromExternal() throws Exception
    {
        DeploymentPlan dp = DeploymentPlan.fromExternal("", env);
    }
}
