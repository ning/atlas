package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

public class InitializedError extends InitializedTemplate
{
    private final String message;

    public InitializedError(Identity id, String type, String name, My my, String message)
    {
        super(id, type, name, my);
        this.message = message;
    }

    @Override
    public Collection<? extends Node> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<InstalledElement> install(ErrorCollector ec, ExecutorService exec, InitializedTemplate root)
    {
        return Futures.immediateFuture((InstalledElement)new InstalledError(getId(), getType(), getName(), getMy(), message));
    }

    @JsonProperty("error")
    public String getError()
    {
        return message;
    }
}
