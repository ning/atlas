package com.ning.atlas;

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

        // initialize

        // install

        // finishDeploy (no one can listen for this yet)

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
                                         environment.getProvisioner(b.getProvisioner())));
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
            try {
                futures.add(p.provision(server, b.getProvisioner(), space, map));
            }
            catch (Exception e) {
                throw new UnsupportedOperationException("Not Yet Implemented!", e);
            }
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
