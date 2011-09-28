package com.ning.atlas.badger;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.Environment;
import com.ning.atlas.ErrorProvisioner;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.base.Either;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeploymentPlan
{
    private final List<Host>  hosts;
    private final SystemMap   map;
    private final Environment environment;
    private final Space space;

    public DeploymentPlan(List<Host> hosts, SystemMap map, Environment environment)
    {
        this.hosts = hosts;
        this.map = map;
        this.environment = environment;
        this.space = Space.emptySpace();
    }

    public void watch(DeploymentObserver observer)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    public Deployment deploy()
    {
        ListeningExecutorService es = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        try {
            provision(es);
//            initialize(es);
        }
        finally {
            es.shutdown();
        }

        return new Deployment(map, space);

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

    public String describe()
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    public static DeploymentPlan fromExternal(String s, Environment env)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
