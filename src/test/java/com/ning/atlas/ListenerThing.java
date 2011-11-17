package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.LifecycleListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class ListenerThing implements LifecycleListener
{

    public static final List<String> calls = new CopyOnWriteArrayList<String>();


    private Future<String> add(String word) {
        calls.add(word);
        return Futures.immediateFuture(word);
    }

    @Override
    public Future<?> startDeployment(Deployment d)
    {
        return add("startDeployment");
    }

    @Override
    public Future<?> startProvision(Deployment d)
    {
        return add("startProvision");
    }

    @Override
    public Future<?> finishProvision(Deployment d)
    {
        return add("finishProvision");
    }

    @Override
    public Future<?> startInit(Deployment d)
    {
        return add("startInit");
    }

    @Override
    public Future<?> finishInit(Deployment d)
    {
        return add("finishInit");
    }

    @Override
    public Future<?> startInstall(Deployment d)
    {
        return add("startInstall");
    }

    @Override
    public Future<?> finishInstall(Deployment d)
    {
        return add("finishInstall");
    }

    @Override
    public Future<?> startUnwind(Deployment d)
    {
        return add("startUnwind");
    }

    @Override
    public Future<?> finishUnwind(Deployment d)
    {
        return add("finishUnwind");
    }

    @Override
    public Future<?> finishDeployment(Deployment d)
    {
        return add("finishDeployment");
    }
}
