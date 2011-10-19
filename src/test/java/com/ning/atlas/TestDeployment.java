package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.noop.NoOpInstaller;
import com.ning.atlas.noop.NoOpProvisioner;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.StepType;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestDeployment
{
    private Environment     env;
    private SystemMap       map;
    private Space           space;
    private NoOpProvisioner provisioner;
    private NoOpInstaller   installer;

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
        child.setInstall(Arrays.asList("foo:install"));
        child.setCardinality(Arrays.asList("a", "b"));
        root.addChild(child);

        this.provisioner = new NoOpProvisioner();
        Map<String, Provisioner> provisioners =
            ImmutableMap.<String, Provisioner>of("noop", provisioner);


        this.installer = new NoOpInstaller();
        Map<String, Installer> installers =
            ImmutableMap.<String, Installer>of("foo", installer);

        env = new Environment("local", provisioners, installers);

        env.addBase(new Base("base",
                             Uri.<Provisioner>valueOf("noop:happy"),
                             Arrays.asList(Uri.<Installer>valueOf("foo:init")),
                             Collections.<String, String>emptyMap()));

        map = root.normalize();
        space = InMemorySpace.newInstance();
    }

    @Test
    public void testDescribe() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
        Description d = dp.describe();

        assertThat(d.getDescriptors().size(), equalTo(2));
        for (HostDeploymentDescription description : d.getDescriptors()) {
            //provisioning
            assertThat(description.getSteps().get(StepType.Provision), equalTo(Arrays.asList("do nothing")));

            // initialization
            assertThat(description.getSteps()
                                  .get(StepType.Initialize), equalTo(Arrays.asList("do nothing with foo:init")));


            assertThat(description.getSteps()
                                  .get(StepType.Install), equalTo(Arrays.asList("do nothing with foo:install")));
        }
    }

    @Test
    public void testPerformHitsProvisioner() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(this.provisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Provisioner>valueOf("noop:happy"))));
        assertThat(this.provisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Provisioner>valueOf("noop:happy"))));
    }

    @Test
    public void testPerformHitsInitializations() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(this.installer.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:init"))));
        assertThat(this.installer.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:init"))));
    }

    @Test
    public void testPerformHitsInstalls() throws Exception
    {
        Deployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(this.installer.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:install"))));
        assertThat(this.installer.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:install"))));
    }
}
