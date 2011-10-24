package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.StepType;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ActualDeployment implements Deployment
{
    private final SystemMap   map;
    private final Environment environment;
    private final Space       space;

    public ActualDeployment(SystemMap map, Environment environment, Space space)
    {
        this.map = map;
        this.environment = environment;
        this.space = space;
    }

    Description describe()
    {
        final Set<Host> servers = map.findLeaves();
        final Map<Host, HostDeploymentDescription> descriptors = Maps.newLinkedHashMap();
        for (Host server : servers) {
            descriptors.put(server, new HostDeploymentDescription(server.getId()));
        }

        List<Pair<Host, Future<String>>> provision_futures = Lists.newArrayList();
        List<Pair<Host, Future<String>>> init_futures = Lists.newArrayList();
        List<Pair<Host, Future<String>>> install_futures = Lists.newArrayList();
        for (Host server : servers) {
            Base base = environment.findBase(server.getBase()).otherwise(Base.errorBase());

            Provisioner p = environment.resolveProvisioner(base.getProvisionUri());
            provision_futures.add(Pair.of(server, p.describe(server, base.getProvisionUri(), this)));

            for (Uri<Installer> uri : base.getInitializations()) {
                Installer i = environment.resolveInstaller(uri);
                init_futures.add(Pair.of(server, i.describe(server, uri, this)));
            }

            for (Uri<Installer> uri : server.getInstallations()) {
                Installer i = environment.resolveInstaller(uri);
                install_futures.add(Pair.of(server, i.describe(server, uri, this)));
            }
        }
        for (Pair<Host, Future<String>> pair : provision_futures) {
            try {
                descriptors.get(pair.getLeft()).addStep(StepType.Provision, pair.getRight().get());
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
        }

        for (Pair<Host, Future<String>> pair : init_futures) {
            try {
                descriptors.get(pair.getLeft()).addStep(StepType.Initialize, pair.getRight().get());
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
        }

        for (Pair<Host, Future<String>> pair : install_futures) {
            try {
                descriptors.get(pair.getLeft()).addStep(StepType.Install, pair.getRight().get());
            }
            catch (InterruptedException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
            }
            catch (ExecutionException e) {
                throw new UnsupportedOperationException("Not Yet Implemented!");
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

        // startDeploy (no one can listen for this yet)

        provision();

        // initializers
        install(new Function<Host, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(Host input)
            {
                final String base_name = input.getBase();
                final Base base = environment.findBase(base_name).otherwise(Base.errorBase());

                final List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
                for (Uri<Installer> uri : base.getInitializations()) {
                    rs.add(Pair.of(uri, environment.resolveInstaller(uri)));
                }
                return rs;
            }
        });

        // installers
        install(new Function<Host, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(Host input)
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

    private void install(Function<Host, List<Pair<Uri<Installer>, Installer>>> f)
    {
        final Set<Host> servers = map.findLeaves();
        final Set<Pair<String, Installer>> installers = Sets.newHashSet();
        final Map<Host, List<Pair<Uri<Installer>, Installer>>> t_to_i = Maps.newHashMap();
        for (Host server : servers) {
            final List<Pair<Uri<Installer>, Installer>> xs = f.apply(server);
            t_to_i.put(server, xs);
            for (Pair<Uri<Installer>, Installer> x : xs) {
                installers.add(Pair.of(x.getLeft().getScheme(), x.getRight()));
            }
        }

        // start
        for (Pair<String, Installer> installer : installers) {
            installer.getRight().start(this);
        }

        // install
        final List<Future<?>> futures = Lists.newArrayList();
        for (Map.Entry<Host, List<Pair<Uri<Installer>, Installer>>> entry : t_to_i.entrySet()) {
            final Host server = entry.getKey();
            for (Pair<Uri<Installer>, Installer> pair : entry.getValue()) {
                final Uri<Installer> uri = pair.getKey();
                final Installer installer = pair.getRight();
                futures.add(installer.install(server, uri, this));
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
            installer.getRight().finish(this);
        }

    }

    private void provision()
    {
        final Set<Host> servers = map.findLeaves();
        final Set<Pair<String, Provisioner>> provisioners = Sets.newHashSet();
        final Map<Host, Pair<Provisioner, Uri<Provisioner>>> s_to_p = Maps.newHashMap();
        for (final Host server : servers) {
            final Maybe<Base> mb = environment.findBase(server.getBase());
            final Base base = mb.otherwise(Base.errorBase());
            Provisioner p = environment.resolveProvisioner(base.getProvisionUri());
            provisioners.add(Pair.of(base.getProvisionUri().getScheme(), p));
            s_to_p.put(server, Pair.of(p, base.getProvisionUri()));
        }

        // startProvision
        for (final Pair<String, Provisioner> provisioner : provisioners) {
            provisioner.getRight().start(this);
        }

        // provision
        final List<Future<?>> futures = Lists.newArrayListWithExpectedSize(servers.size());
        for (final Host server : servers) {
            final Pair<Provisioner, Uri<Provisioner>> pair = s_to_p.get(server);
            final Provisioner p = pair.getLeft();
            futures.add(p.provision(server, pair.getRight(), this));
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
            provisioner.getRight().finish(this);
        }
    }

    public SystemMap getSystemMap()
    {
        return map;
    }

    public Environment getEnvironment()
    {
        return environment;
    }

    public Space getSpace()
    {
        return space;
    }
}
