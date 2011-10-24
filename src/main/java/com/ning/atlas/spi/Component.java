package com.ning.atlas.spi;

public interface Component
{
    public void start(Deployment deployment);
    public void finish(Deployment deployment);
}

