package com.ning.atlas.spi;

import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseComponent implements Component
{
    private final AtomicReference<Deployment> deployment = new AtomicReference<Deployment>();

    protected Deployment getDeployment() {
        return deployment.get();
    }

    @Override
    public final void start(Deployment deployment)
    {
        this.deployment.set(deployment);
        startLocal(deployment);
    }

    protected void startLocal(Deployment deployment) {}

    @Override
    public final void finish(Deployment deployment)
    {
        this.deployment.set(null);
        finishLocal(deployment);
    }

    protected void finishLocal(Deployment deployment) {}
}
