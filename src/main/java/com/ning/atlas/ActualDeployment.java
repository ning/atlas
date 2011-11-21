package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.space.Space;
import org.apache.commons.lang3.tuple.Pair;

import java.security.SecureRandomSpi;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Lists.transform;

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
                Installer i = environment.resolveInstaller(uri.getScheme());
                init_futures.add(Pair.of(server, i.describe(server, uri, this)));
            }

            for (Uri<Installer> uri : server.getInstallations()) {
                Installer i = environment.resolveInstaller(uri.getScheme());
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

    public void destroy()
    {
        ListeningExecutorService es = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        List<LifecycleListener> listeners = createListeners();

        startDeployment(listeners);
        unwind(listeners, es);
        finishDeployment(listeners);

        es.shutdown();
    }

    public void update()
    {
        ListeningExecutorService es = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        List<LifecycleListener> listeners = createListeners();

        startDeployment(listeners);
        provision(listeners);
        initialize(listeners, es);
        install(listeners, es);
        unwind(listeners, es);
        finishDeployment(listeners);

        es.shutdown();
    }

    private void finishDeployment(List<LifecycleListener> listeners)
    {
        for (Host host : map.findLeaves()) {
            try {
                WhatWasDone wwd = new WhatWasDone();
                Base b = environment.findBase(host.getBase()).getValue();
                wwd.setProvisioner(b.getProvisionUri());
                wwd.setInitializations(transform(b.getInitializations(), Uri.urisToStrings()));
                wwd.setInstallations(transform(host.getInstallations(), Uri.urisToStrings()));
                space.store(host.getId().createChild("atlas", "unwind"), wwd);
            }
            catch (Exception e) {
                log.warn(e, "broke trying to record unwind data for a host %s", host.getId());
            }
        }

        // finishDeploy (no one can listen for this yet)
        fire(Events.finishDeployment, listeners);
    }

    private void startDeployment(List<LifecycleListener> listeners) {fire(Events.startDeployment, listeners);}

    private List<LifecycleListener> createListeners()
    {
        return Lists.transform(environment.getListeners(), new Function<Pair<Class<? extends LifecycleListener>, Map<String, String>>, LifecycleListener>()
        {
            @Override
            public LifecycleListener apply(Pair<Class<? extends LifecycleListener>, Map<String, String>> input)
            {
                checkNotNull(input);
                try {
                    return Instantiator.create(input.getKey(), input.getValue());
                }
                catch (Exception e) {
                    throw new IllegalStateException("unable to instantiate listener " + input.getKey().getName(), e);
                }
            }
        });
    }

    private void unwind(List<LifecycleListener> listeners, ListeningExecutorService es)
    {
        log.info("beginning unwind");
        fire(Events.startUnwind, listeners);


        final Cache<String, Installer> installer_cache = CacheBuilder.newBuilder()
                                                                     .maximumSize(Integer.MAX_VALUE)
                                                                     .concurrencyLevel(10)
                                                                     .removalListener(new RemovalListener<String, Installer>()
                                                                     {
                                                                         @Override
                                                                         public void onRemoval(RemovalNotification<String, Installer> event)
                                                                         {
                                                                             event.getValue()
                                                                                  .finish(ActualDeployment.this);
                                                                         }
                                                                     })
                                                                     .build(new CacheLoader<String, Installer>()
                                                                     {
                                                                         @Override
                                                                         public Installer load(String key) throws Exception
                                                                         {
                                                                             Installer installer = environment.resolveInstaller(key);
                                                                             installer.start(ActualDeployment.this);
                                                                             return installer;
                                                                         }
                                                                     });

        final Cache<String, Provisioner> provisioner_cache = CacheBuilder.newBuilder()
                                                                         .maximumSize(Integer.MAX_VALUE)
                                                                         .concurrencyLevel(10)
                                                                         .removalListener(new RemovalListener<String, Provisioner>()
                                                                         {
                                                                             @Override
                                                                             public void onRemoval(RemovalNotification<String, Provisioner> event)
                                                                             {
                                                                                 event.getValue()
                                                                                      .finish(ActualDeployment.this);
                                                                             }
                                                                         })
                                                                         .build(new CacheLoader<String, Provisioner>()
                                                                         {
                                                                             @Override
                                                                             public Provisioner load(String key) throws Exception
                                                                             {
                                                                                 Provisioner p = environment.resolveProvisioner(key);
                                                                                 p.start(ActualDeployment.this);
                                                                                 return p;
                                                                             }
                                                                         });

        List<Future<?>> futures = Lists.newArrayList();
        final Set<Identity> deployed = Sets.newHashSet();
        for (Host host : map.findLeaves()) {
            deployed.add(host.getId());
        }
        Set<Identity> identities = space.findAllIdentities();
        for (final Identity identity : identities) {
            final Maybe<WhatWasDone> mwwd = space.get(identity.createChild("atlas", "unwind"),
                                                      WhatWasDone.class,
                                                      Missing.RequireAll);

            if (!deployed.contains(identity) && mwwd.isKnown()) {
                futures.add(es.submit(new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        WhatWasDone wwd = mwwd.getValue();

                        for (Uri<Installer> in : transform(reverse(wwd.getInstallations()), Uri.<Installer>stringToUri())) {
                            try {
                                installer_cache.get(in.getScheme())
                                               .uninstall(identity, in, ActualDeployment.this)
                                               .get();
                            }
                            catch (Exception e) {
                                log.warn(e, "unable to unwind %s on %s", in.toString(), identity.toExternalForm());
                            }
                        }

                        for (Uri<Installer> in : transform(reverse(wwd.getInitializations()), Uri.<Installer>stringToUri())) {
                            try {
                                installer_cache.get(in.getScheme())
                                               .uninstall(identity, in, ActualDeployment.this)
                                               .get();
                            }
                            catch (Exception e) {
                                log.warn(e, "unable to unwind %s on %s", in.toString(), identity.toExternalForm());
                            }
                        }

                        try {
                            provisioner_cache.get(wwd.getProvisioner().getScheme())
                                             .destroy(identity,
                                                      wwd.getProvisioner(),
                                                      ActualDeployment.this).get();
                        }
                        catch (Exception e) {
                            log.warn(e, "unable to destroy %s on %s",
                                     wwd.getProvisioner().toString(),
                                     identity.toExternalForm());
                        }

                        space.deleteAll(identity);
                        return null;
                    }
                }));
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (Exception e) {
                log.warn(e, "Exception while unwindind");
            }
        }


        installer_cache.invalidateAll();
        provisioner_cache.invalidateAll();
        log.info("finished unwind");
        fire(Events.finishUnwind, listeners);
    }

    private void install(List<LifecycleListener> listeners, ListeningExecutorService es)
    {
        log.info("starting install");
        fire(Events.startInstall, listeners);
        performInstalls(es, new Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>>()
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
                        i = environment.resolveInstaller(uri.getScheme());
                        input.getRight().put(uri.getScheme(), i);
                    }

                    rs.add(Pair.of(uri, i));
                }
                return rs;
            }
        });
        fire(Events.finishInstall, listeners);
        log.info("finished install");
    }

    private void initialize(List<LifecycleListener> listeners, ListeningExecutorService es)
    {
        log.info("starting init");
        fire(Events.startInit, listeners);
        performInstalls(es, new Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>>()
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
                        i = environment.resolveInstaller(uri.getScheme());
                        input.getRight().put(uri.getScheme(), i);
                    }
                    rs.add(Pair.of(uri, i));
                }
                return rs;
            }
        });
        fire(Events.finishInit, listeners);
        log.info("finished init");
    }

    public static class WhatWasDone
    {
        private Uri<Provisioner> provisioner;
        private List<String>     initializations;
        private List<String>     installations;

        public Uri<Provisioner> getProvisioner()
        {
            return provisioner;
        }

        public void setProvisioner(Uri<Provisioner> provisioner)
        {
            this.provisioner = provisioner;
        }

        public List<String> getInitializations()
        {
            return initializations;
        }

        public void setInitializations(List<String> initializations)
        {
            this.initializations = initializations;
        }

        public List<String> getInstallations()
        {
            return installations;
        }

        public void setInstallations(List<String> installations)
        {
            this.installations = installations;
        }
    }

    private void performInstalls(ListeningExecutorService es,
                                 Function<Pair<Host, Map<String, Installer>>, List<Pair<Uri<Installer>, Installer>>> f)
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
            final List<Pair<Uri<Installer>, Installer>> installations = entry.getValue();
            futures.add(installAllOnHost(es, server, installations));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (Exception e) {
                log.warn(e, "Exception trying to execute installation");
                throw new UnsupportedOperationException("Not Yet Implemented!", e);
            }
        }

        // finish
        for (Installer installer : installers.values()) {
            installer.finish(this);
        }

    }

    private Future<?> installAllOnHost(ListeningExecutorService es,
                                       final Host server,
                                       final List<Pair<Uri<Installer>, Installer>> installations)
    {
        return es.submit(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                log.info("installing on %s : %s", server.getId(), installations);
                for (Pair<Uri<Installer>, Installer> installation : installations) {
                    log.info("installing %s on %s", installation.getKey().toString(), server.getId());
                    installation.getValue().install(server, installation.getKey(), ActualDeployment.this).get();
                }
                return null;
            }
        });
    }

    private void provision(List<LifecycleListener> listeners)
    {
        log.info("starting provision");
        fire(Events.startProvision, listeners);

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
        for (Provisioner provisioner : provisioners.values()) {
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
        for (Provisioner provisioner : provisioners.values()) {
            provisioner.finish(this);
        }
        fire(Events.finishProvision, listeners);
        log.info("finished provision");
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

    private void fire(Events event, List<LifecycleListener> listeners)
    {
        List<Future<?>> fs = Lists.newArrayList();
        for (LifecycleListener listener : listeners) {
            fs.add(event.fire(listener, this));
        }
        for (Future<?> f : fs) {
            try {
                f.get();
            }
            catch (Exception e) {
                log.warn(e, "exception from listener");
            }
        }
    }

    private static enum Events
    {
        startDeployment()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.startDeployment(d);
                }
            },
        startProvision()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.startProvision(d);
                }
            },
        finishProvision()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.finishProvision(d);
                }
            },
        startInit()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.startInit(d);
                }
            },
        finishInit()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.finishInit(d);
                }
            },
        startInstall()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.startInstall(d);
                }
            },
        finishInstall()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.finishInstall(d);
                }
            },
        finishDeployment()
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.finishDeployment(d);
                }
            },
        startUnwind
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.startUnwind(d);
                }
            },
        finishUnwind
            {
                @Override
                public Future<?> fire(LifecycleListener listener, Deployment d)
                {
                    return listener.finishUnwind(d);
                }
            };


        public abstract Future<?> fire(LifecycleListener listener, Deployment d);


    }
}
