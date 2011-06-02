package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;

public interface Initializer
{
    ListenableFuture<Server> initialize(Server server);
}
