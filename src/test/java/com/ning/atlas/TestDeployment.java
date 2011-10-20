package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.noop.NoOpInstaller;
import com.ning.atlas.noop.NoOpProvisioner;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.StepType;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestDeployment
{
    private Environment env;
    private SystemMap   map;
    private Space       space;

    @Before
    public void setUp() throws Exception
    {
        ServerTemplate child = new ServerTemplate("child",
                                                  "base",
                                                  asList("a", "b"),
                                                  asList(Uri.<Installer>valueOf("foo:install")),
                                                  Collections.<String, Object>emptyMap());

        SystemTemplate root = new SystemTemplate("root",
                                                 Collections.<String, Object>emptyMap(),
                                                 asList("0"),
                                                 Arrays.<Template>asList(child));

        NoOpProvisioner.reset();
        Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners =
            ImmutableMap.of("noop", Pair.<Class<? extends Provisioner>, Map<String, String>>of(NoOpProvisioner.class, Collections
                .<String, String>emptyMap()));

        NoOpInstaller.reset();
        Map<String, Pair<Class<? extends Installer>, Map<String, String>>> installers =
            ImmutableMap.of("foo", Pair.<Class<? extends Installer>, Map<String, String>>of(NoOpInstaller.class, Collections
                .<String, String>emptyMap()));

        Map<String, Base> bases = ImmutableMap.of("base", new Base(Uri.<Provisioner>valueOf("noop:happy"),
                                                                   Arrays.asList(Uri.<Installer>valueOf("foo:init"))));

        env = new Environment(provisioners, installers, bases, Collections.<String, String>emptyMap());

        map = root.normalize();
        space = InMemorySpace.newInstance();
    }

    @Test
    public void testDescribe() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
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
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(NoOpProvisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Provisioner>valueOf("noop:happy"))));
        assertThat(NoOpProvisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Provisioner>valueOf("noop:happy"))));
    }

    @Test
    public void testPerformHitsInitializations() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:init"))));
        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:init"))));
    }

    @Test
    public void testPerformHitsInstalls() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.perform();

        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:install"))));
        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:install"))));
    }
}
