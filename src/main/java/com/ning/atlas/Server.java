package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;

public interface Server
{
    public String getExternalIpAddress();
    public String getInternalIpAddress();

    ListenableFuture<? extends Server> initialize();
}
