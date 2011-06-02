package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;

public class ErrorInitializer implements Initializer
{
    @Override
    public ListenableFuture<Server> initialize(Server server)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
