package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.components.noop.NoOpInstaller;
import com.ning.atlas.components.noop.NoOpProvisioner;
import com.ning.atlas.plugin.StaticPluginSystem;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestActualDeployment
{
    private Environment env;
    private SystemMap   map;
    private Space space;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception
    {
        ServerTemplate child = new ServerTemplate("child",
                                                  Uri.<Base>valueOf("base"),
                                                  asList("a", "b"),
                                                  asList(Uri.<Installer>valueOf("foo:install")),
                                                  Collections.<String, Object>emptyMap());

        SystemTemplate root = new SystemTemplate("root",
                                                 Collections.<String, Object>emptyMap(),
                                                 asList("0"),
                                                 Arrays.<Template>asList(child));
        StaticPluginSystem plugins = new StaticPluginSystem();
        NoOpProvisioner.reset();
        plugins.registerProvisioner("noop", NoOpProvisioner.class, Collections.<String, String>emptyMap());

        NoOpInstaller.reset();
        plugins.registerInstaller("foo", NoOpInstaller.class, Collections.<String, String>emptyMap());

        Map<String, Base> bases = ImmutableMap.of("base", new Base(Uri.<Provisioner>valueOf("noop:happy"),
                                                                   Arrays.asList(Uri.<Installer>valueOf("foo:init"))));



        env = new Environment("hello",
                              plugins,
                              Collections.<String, Map<String,String>>emptyMap(),
                              Collections.<String, Map<String,String>>emptyMap(),
                              Collections.<String, Map<String,String>>emptyMap(),
                              bases,
                              Collections.<String, String>emptyMap());

        map = root.normalize(env);
        space = InMemorySpace.newInstance();
    }

    @Test
    public void testPerformHitsProvisioner() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.converge();

        assertThat(NoOpProvisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Provisioner>valueOf("noop:happy"))));
        assertThat(NoOpProvisioner.getProvisioned(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Provisioner>valueOf("noop:happy"))));
    }

    @Test
    public void testPerformHitsInitializations() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.converge();

        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:init"))));
        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:init"))));
    }

    @Test
    public void testPerformHitsInstalls() throws Exception
    {
        ActualDeployment dp = env.planDeploymentFor(map, space);
        dp.converge();

        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.a"), Uri.<Installer>valueOf("foo:install"))));
        assertThat(NoOpInstaller.getInstalled(),
                   hasItem(Pair.of(Identity.valueOf("/root.0/child.b"), Uri.<Installer>valueOf("foo:install"))));
    }
}
