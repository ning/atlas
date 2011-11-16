package com.ning.atlas;

import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ConcurrentComponent extends BaseComponent implements Provisioner, Installer
{

    private static final Logger log = Logger.get(ConcurrentComponent.class);

    private final ExecutorService threadPool;

    public ConcurrentComponent()
    {
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public final Future<Status> install(final Host server, final Uri<Installer> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<Status>()
        {

            @Override
            public Status call() throws Exception
            {
                try {
                    return Status.okay(perform(server, uri, deployment));
                }
                catch (Exception e) {
                    log.warn(e, "exception performing actual installation");
                    return Status.fail(e.getMessage());
                }
            }
        });
    }

    @Override
    public final Future<Status> provision(final Host node, final Uri<Provisioner> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<Status>()
        {

            @Override
            public Status call() throws Exception
            {
                try {
                    return Status.okay(perform(node, uri, deployment));
                }
                catch (Exception e) {
                    log.warn(e, "exception performing provision");
                    return Status.fail(e.getMessage());
                }

            }
        });
    }

    @Override
    public Future<Status> uninstall(final Identity hostId, final Uri<Installer> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<Status>()
        {
            @Override
            public Status call() throws Exception
            {
                try {
                    return Status.okay(unwind(hostId, uri, deployment));
                }
                catch (Exception e) {
                    log.warn(e, "failed to unwind %s on %s", uri, hostId);
                    return Status.fail(e.getMessage());
                }
            }
        });
    }

    @Override
    public Future<Status> destroy(final Identity hostId, final Uri<Provisioner> uri, final Deployment deployment)
    {
        return threadPool.submit(new Callable<Status>()
        {
            @Override
            public Status call() throws Exception
            {
                try {
                    return Status.okay(unwind(hostId, uri, deployment));
                }
                catch (Exception e) {
                    log.warn(e, "failed to unwind %s on %s", uri, hostId);
                    return Status.fail(e.getMessage());
                }
            }
        });
    }

    public abstract String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception;

    public abstract String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception;

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
