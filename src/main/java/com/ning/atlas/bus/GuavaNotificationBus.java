package com.ning.atlas.bus;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ning.atlas.spi.bus.NotificationBus;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GuavaNotificationBus implements NotificationBus
{
    private final AtomicReference<AsyncEventBus> stageBus = new AtomicReference<AsyncEventBus>();

    private final AsyncEventBus bus = new AsyncEventBus(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                                                          .setDaemon(true)
                                                                                          .setNameFormat("event-bus-%d")
                                                                                          .build()));

    @Override
    public void subscribe(Scope scope, Object listener)
    {
        if (scope == Scope.Stage) {
            stageBus.get().register(listener);
        }
        else {
            bus.register(listener);
        }
    }

    @Override
    public void unsubscribe(Object listener)
    {
        bus.unregister(listener);
        stageBus.get().unregister(listener);
    }

    public void post(Object event) {
        bus.post(event);
        stageBus.get().post(event);
    }

    public void startNewStage()
    {
        AsyncEventBus bus = stageBus.get();
        if (bus != null) {
            bus.getExecutor().shutdown();
        }
        stageBus.set(new AsyncEventBus(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                                         .setDaemon(true)
                                                                         .setNameFormat("stage-event-bus-%d")
                                                                         .build())));
    }
}


