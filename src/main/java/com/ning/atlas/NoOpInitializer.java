package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class NoOpInitializer implements Initializer
{
    @Override
    public ListenableFuture<Server> initialize(Server server)
    {
        return Futures.immediateFuture(server);
    }
}
