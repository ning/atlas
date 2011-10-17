package com.ning.atlas.badger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ning.atlas.Base;
import com.ning.atlas.Description;
import com.ning.atlas.HostDeploymentDescription;
import com.ning.atlas.Result;
import com.ning.atlas.Deployment;
import com.ning.atlas.Environment;
import com.ning.atlas.ServerTemplate;
import com.ning.atlas.Space;
import com.ning.atlas.SystemMap;
import com.ning.atlas.SystemTemplate;
import com.ning.atlas.Uri;
import com.ning.atlas.noop.NoOpInstaller;
import com.ning.atlas.noop.NoOpProvisioner;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.StepType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestNewStyleStuff
{
    private Environment env;
    private SystemMap   map;
    private Result      emptyDeployment;
    private Space       space;

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

        Map<String, Provisioner> provisioners =
            ImmutableMap.<String, Provisioner>of("noop", new NoOpProvisioner());

        Map<String, Installer> installers =
            ImmutableMap.<String, Installer>of("foo", new NoOpInstaller());

        env = new Environment("local", provisioners, installers);

        env.addBase(new Base("base",
                             Uri.<Provisioner>valueOf("noop"),
                             Arrays.asList(Uri.<Installer>valueOf("foo:say-hello")),
                             Collections.<String, String>emptyMap()));

        map = root.normalize();

        emptyDeployment = Result.nil();

        space = Space.emptySpace();
    }

    @Test
    public void testApiDesign() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
        Description d = dp.describe();

        assertThat(d.getDescriptors().size(), equalTo(2));
        for (HostDeploymentDescription description : d.getDescriptors()) {
            //provisioning
            assertThat(description.getSteps().get(StepType.Provision), equalTo(Arrays.asList("do nothing")));

            // initialization
            assertThat(description.getSteps()
                                  .get(StepType.Initialize), equalTo(Arrays.asList("do nothing with foo:say-hello")));


            assertThat(description.getSteps()
                                  .get(StepType.Install), equalTo(Arrays.asList("do nothing with foo:bar")));


        }


    }

    @Test
    public void testFoo() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
    }

    @Test
    @Ignore
    public void testFromExternal() throws Exception
    {
        Deployment dp = Deployment.fromExternal("", env);
    }
}
