package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.StepType;
import org.apache.commons.lang3.tuple.Pair;
import sun.jvm.hotspot.asm.sparc.SPARCTrapInstruction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Deployment
{
    private final SystemMap   map;
    private final Environment environment;
    private final Space       space;

    public Deployment(SystemMap map, Environment environment, Space space)
    {
        this.map = map;
        this.environment = environment;
        this.space = space;
    }

    public Description describe()
    {
        final Set<NormalizedServerTemplate> servers = map.findLeaves();
        final Map<NormalizedServerTemplate, HostDeploymentDescription> descriptors = Maps.newLinkedHashMap();
        for (NormalizedServerTemplate server : servers) {
            descriptors.put(server, new HostDeploymentDescription(server.getId()));
        }

        for (NormalizedServerTemplate server : servers) {
            Base base = environment.findBase(server.getBase()).otherwise(Base.errorBase(server.getBase(), environment));

            Provisioner p = environment.resolveProvisioner(base.getProvisioner());
            descriptors.get(server).addStep(StepType.Provision, p.describe(server, base.getProvisioner(), space));

            for (Uri<Installer> uri : base.getInitializations()) {
                Installer i = environment.resolveInstaller(uri);
                descriptors.get(server).addStep(StepType.Initialize, i.describe(server, uri, space));
            }

            for (Uri<Installer> uri : server.getInstallations()) {
                Installer i = environment.resolveInstaller(uri);
                descriptors.get(server).addStep(StepType.Install, i.describe(server, uri, space));
            }

        }

        return new Description(descriptors.values());
    }

    public void perform()
    {
        // perform

        /**
         * lifecycle: startDeploy ->
         *            startProvision -> provision[] -> finishProvision ->
         *            startInitialize -> initialize[] -> finishInitialize ->
         *            startInstall -> install[] -> finishInstall ->
         *            finishDeploy
         *
         *            start[action] is fired once per deployment
         *            [action] is fired once per action
         *            finish[action] is fired once per deployment
         *
         *            the idea is to be able to group together things, cosmos for instance
         *            could accumulate everything being changed and on finishInstall actually
         *            run cosmos. This, of course, means that finishInstall needs to be able to
         *            report per-server failures, which can be a tricky alignment issue, I think.
         *
         *            This model implies we need some per-deployment state variable which can accumulate
         *            stuff across stages in a given provisioner/installer. We could say "one component instance
         *            per deployment stage (ie, same type, diff installer for init vs install)" and let it accumulate
         *            whatever state it wants.
         */

        // find all components

        // startDeploy (no one can listen for this yet)

        provision();

        // initializers
        install(new Function<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(NormalizedServerTemplate input)
            {
                final String base_name = input.getBase();
                final Base base = environment.findBase(base_name).otherwise(Base.errorBase(base_name, environment));

                final List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
                for (Uri<Installer> uri : base.getInitializations()) {
                    rs.add(Pair.of(uri, environment.resolveInstaller(uri)));
                }
                return rs;
            }
        });

        // installers
        install(new Function<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(NormalizedServerTemplate input)
            {
                final List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
                for (Uri<Installer> uri : input.getInstallations()) {
                    rs.add(Pair.of(uri, environment.resolveInstaller(uri)));
                }
                return rs;
            }
        });

        // finishDeploy (no one can listen for this yet)

    }

    private void install(Function<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>> f)
    {
        final Set<NormalizedServerTemplate> servers = map.findLeaves();
        final Set<Pair<String, Installer>> installers = Sets.newHashSet();
        final Map<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>> t_to_i = Maps.newHashMap();
        for (NormalizedServerTemplate server : servers) {
            final List<Pair<Uri<Installer>, Installer>> xs = f.apply(server);
            t_to_i.put(server, xs);
            for (Pair<Uri<Installer>, Installer> x : xs) {
                installers.add(Pair.of(x.getLeft().getScheme(), x.getRight()));
            }
        }

        // start
        for (Pair<String, Installer> installer : installers) {
            installer.getRight().start(map, space);
        }

        // install
        final List<Future<?>> futures = Lists.newArrayList();
        for (Map.Entry<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>> entry : t_to_i.entrySet()) {
            final NormalizedServerTemplate server = entry.getKey();
            for (Pair<Uri<Installer>, Installer> pair : entry.getValue()) {
                final Uri<Installer> uri = pair.getKey();
                final Installer installer = pair.getRight();
                futures.add(installer.install(server, uri, space, map));
            }
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
        }

        // finish
        for (Pair<String, Installer> installer : installers) {
            installer.getRight().finish(map, space);
        }

    }

    private void initialize()
    {
        final Set<NormalizedServerTemplate> servers = map.findLeaves();
        final Set<Pair<String, Installer>> installers = Sets.newHashSet();
        final Map<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>> t_to_i = Maps.newHashMap();

        for (NormalizedServerTemplate server : servers) {
            // wish java could have default values for map entries
            t_to_i.put(server, Lists.<Pair<Uri<Installer>, Installer>>newArrayList());
        }

        for (final NormalizedServerTemplate server : servers) {
            final Maybe<Base> mb = environment.findBase(server.getBase());
            if (mb.isKnown()) {
                final Base b = mb.getValue();
                for (Uri<Installer> uri : b.getInitializations()) {
                    Installer installer = environment.resolveInstaller(uri);
                    installers.add(Pair.of(uri.getScheme(), installer));
                    t_to_i.get(server).add(Pair.of(uri, installer));
                }
            }
        }

        // startInit
        for (Pair<String, Installer> installer : installers) {
            installer.getRight().start(map, space);
        }

        // init
        final List<Future<?>> futures = Lists.newArrayList();
        for (Map.Entry<NormalizedServerTemplate, List<Pair<Uri<Installer>, Installer>>> entry : t_to_i.entrySet()) {
            final NormalizedServerTemplate server = entry.getKey();
            for (Pair<Uri<Installer>, Installer> pair : entry.getValue()) {
                final Uri<Installer> uri = pair.getKey();
                final Installer installer = pair.getRight();
                futures.add(installer.install(server, uri, space, map));
            }
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
        }

        // finishInit
        for (Pair<String, Installer> installer : installers) {
            installer.getRight().finish(map, space);
        }
    }

    private void provision()
    {
        final Set<NormalizedServerTemplate> servers = map.findLeaves();
        final Set<Pair<String, Provisioner>> provisioners = Sets.newHashSet();
        final Map<NormalizedServerTemplate, Base> bases = Maps.newHashMap();
        for (final NormalizedServerTemplate server : servers) {
            final Maybe<Base> mb = environment.findBase(server.getBase());
            bases.put(server, mb.otherwise(Base.errorBase(server.getBase(), environment)));
            if (mb.isKnown()) {
                final Base b = mb.getValue();
                provisioners.add(Pair.of(b.getProvisioner().getScheme(),
                                         environment.resolveProvisioner(b.getProvisioner())));
            }
        }

        // startProvision
        for (final Pair<String, Provisioner> provisioner : provisioners) {
            provisioner.getRight().start(map, space);
        }

        // provision
        final List<Future<?>> futures = Lists.newArrayListWithExpectedSize(servers.size());
        for (final NormalizedServerTemplate server : servers) {
            final Base b = bases.get(server);
            final Provisioner p = environment.getProvisioner(b.getProvisioner());
            futures.add(p.provision(server, b.getProvisioner(), space, map));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!", e);
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!", e);
            }
        }

        // finishProvision
        for (Pair<String, Provisioner> provisioner : provisioners) {
            provisioner.getRight().finish(map, space);
        }
    }
}
