package com.ning.atlas.spi;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class BaseLifecycleListener implements LifecycleListener
{
    private static final FutureTask<Object> NO_OP  = new FutureTask<Object>(new Runnable() {
        @Override
        public void run()
        {
        }
    }, new Object());

    static {
        NO_OP.run();
    }

    @Override
    public Future<?> startDeployment(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> startProvision(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> finishProvision(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> startInit(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> finishInit(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> startInstall(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> finishInstall(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> startUnwind(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> finishUnwind(Deployment d)
    {
        return NO_OP;
    }

    @Override
    public Future<?> finishDeployment(Deployment d)
    {
        return NO_OP;
    }
}
