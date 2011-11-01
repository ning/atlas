package com.ning.atlas.spi;

import java.util.concurrent.Future;

public interface LifecycleListener
{
    public Future<?> startDeployment(Deployment d);
    public Future<?> startProvision(Deployment d);
    public Future<?> finishProvision(Deployment d);
    public Future<?> startInit(Deployment d);
    public Future<?> finishInit(Deployment d);
    public Future<?> startInstall(Deployment d);
    public Future<?> finishInstall(Deployment d);
    public Future<?> finishDeployment(Deployment d);
}
