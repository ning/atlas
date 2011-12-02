package com.ning.atlas.spi.bus;

public interface NotificationBus
{
    public void subscribe(Scope scope, Object listener);

    public void unsubscribe(Object listener);

    public static enum Scope {
        Stage, Deployment
    }
}
