package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.logging.Logger;
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
    private static final Logger log = Logger.get(ActualDeployment.class);

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

            Provisioner p = environment.resolveProvisioner(base.getProvisionUri().getScheme());
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

        log.info("starting provision");
        provision();
        log.info("finished provision");

        // initializers
        log.info("starting init");
        install(new Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(Pair<Host, Map<String, Installer>> input)
            {

                final String base_name = input.getLeft().getBase();
                final Base base = environment.findBase(base_name).otherwise(Base.errorBase());

                final List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
                for (Uri<Installer> uri : base.getInitializations()) {
                    Installer i;
                    if (input.getRight().containsKey(uri.getScheme())) {
                        i = input.getRight().get(uri.getScheme());
                    }
                    else {
                        i = environment.resolveInstaller(uri);
                        input.getRight().put(uri.getScheme(), i);
                    }
                    rs.add(Pair.of(uri, i));
                }
                return rs;
            }
        });
        log.info("finished init");

        // installers
        log.info("starting install");
        install(new Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>>()
        {
            @Override
            public List<Pair<Uri<Installer>, Installer>> apply(Pair<Host, Map<String, Installer>> input)
            {
                final List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
                for (Uri<Installer> uri : input.getLeft().getInstallations()) {
                    Installer i;
                    if (input.getRight().containsKey(uri.getScheme())) {
                        i = input.getRight().get(uri.getScheme());
                    }
                    else {
                        i = environment.resolveInstaller(uri);
                        input.getRight().put(uri.getScheme(), i);
                    }

                    rs.add(Pair.of(uri, i));
                }
                return rs;
            }
        });
        log.info("finished install");

        // finishDeploy (no one can listen for this yet)

    }

    private void install(Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>> f)
    {
        final Map<String, Installer> installers = Maps.newHashMap();
        final Set<Host> servers = map.findLeaves();
        final Map<Host, List<Pair<Uri<Installer>, Installer>>> t_to_i = Maps.newHashMap();
        for (Host server : servers) {
            final List<Pair<Uri<Installer>, Installer>> xs = f.apply(Pair.of(server, installers));
            t_to_i.put(server, xs);
        }

        // start
        for (Installer installer : installers.values()) {
            installer.start(this);
        }

        // install
        final List<Future<?>> futures = Lists.newArrayList();
        for (Map.Entry<Host, List<Pair<Uri<Installer>, Installer>>> entry : t_to_i.entrySet()) {
            final Host server = entry.getKey();
            for (Pair<Uri<Installer>, Installer> pair : entry.getValue()) {
                final Uri<Installer> uri = pair.getKey();
                final Installer installer = pair.getRight();
                log.info("installing %s on %s", uri, server.getId());
                futures.add(installer.install(server, uri, this));
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

        // finish
        for (Installer installer : installers.values()) {
            installer.finish(this);
        }

    }

    private void provision()
    {
        final Set<Host> servers = map.findLeaves();
        final Map<String, Provisioner> provisioners = Maps.newHashMap();
        final Map<Host, Pair<Provisioner, Uri<Provisioner>>> s_to_p = Maps.newHashMap();
        for (final Host server : servers) {
            final Maybe<Base> mb = environment.findBase(server.getBase());
            final Base base = mb.otherwise(Base.errorBase());
            final String scheme = base.getProvisionUri().getScheme();
            if (!provisioners.containsKey(scheme)) {
                Provisioner p = environment.resolveProvisioner(base.getProvisionUri().getScheme());
                provisioners.put(scheme, p);
            }
            Provisioner p = provisioners.get(scheme);

            s_to_p.put(server, Pair.of(p, base.getProvisionUri()));
        }

        // startProvision
        for (Provisioner provisioner : provisioners.values()  ) {
            provisioner.start(this);
        }

        // provision
        final List<Future<?>> futures = Lists.newArrayListWithExpectedSize(servers.size());
        for (final Host server : servers) {
            final Pair<Provisioner, Uri<Provisioner>> pair = s_to_p.get(server);
            final Provisioner p = pair.getLeft();
            log.info("provisioning %s for %s", pair.getRight(), server.getId());
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
        for (Provisioner provisioner : provisioners.values()  ) {
            provisioner.finish(this);
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