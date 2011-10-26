package com.ning.atlas;

import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ConcurrentComponent<T> extends BaseComponent implements Provisioner, Installer
{

    private final ExecutorService threadPool;

    public ConcurrentComponent()
    {
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public final Future<T> install(final Host server, final Uri<Installer> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<T>()
        {

            @Override
            public T call() throws Exception
            {
                return perform(server, uri, deployment);
            }
        });
    }

    @Override
    public final Future<T> provision(final Host node, final Uri<Provisioner> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<T>()
        {

            @Override
            public T call() throws Exception
            {
                return perform(node, uri, deployment);
            }
        });
    }

    public abstract T perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception;

    @Override
    protected final void finishLocal(Deployment deployment)
    {
        threadPool.shutdown();
        finishLocal2(deployment);
    }

    protected void finishLocal2(Deployment deployment)
    {
        // NOOP
    }
}
