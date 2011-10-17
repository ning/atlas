package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.StepType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Deployment
{
    private final List<Host>  hosts;
    private final SystemMap   map;
    private final Environment environment;
    private final Space       space;

    public Deployment(List<Host> hosts, SystemMap map, Environment environment)
    {
        this.hosts = hosts;
        this.map = map;
        this.environment = environment;
        this.space = Space.emptySpace();
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

    public Result perform()
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


        ListeningExecutorService es = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        try {
            provision(es);
//            initialize(es);
        }
        finally {
            es.shutdown();
        }

        return new Result(map, space);

    }

    private void provision(ListeningExecutorService es)
    {
        final List<Pair<Host, ListenableFuture<Server>>> futures = Lists.newArrayList();
        for (final Host host : hosts) {
            final Maybe<Provisioner> m_prov = environment.findProvisioner(host.getProvisioner());
            ListenableFuture<Server> f = es.submit(new Callable<Server>()
            {
                @Override
                public Server call() throws UnableToProvisionServerException
                {
                    final Provisioner p = m_prov.otherwise(new ErrorProvisioner());
                    return p.provision(host.getBase(), map.getSingleRoot());
                }
            });
            futures.add(Pair.of(host, f));
        }

        for (Pair<Host, ListenableFuture<Server>> future : futures) {
            try {
                Server s = future.getRight().get();
                future.getLeft().setServer(s);
            }
            catch (Exception e) {
                future.getLeft().addError(e);
            }
        }
    }

    private void initialize(ListeningExecutorService es)
    {

    }
}
