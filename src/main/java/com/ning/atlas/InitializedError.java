package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;

public class InitializedError extends InitializedTemplate
{
    private final String message;

    public InitializedError(String type, String name, My my, String message)
    {
        super(type, name, my);
        this.message = message;
    }

    @Override
    public Collection<? extends Thing> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<InstalledError> install(Executor exec, InitializedTemplate root)
    {
        return Futures.immediateFuture(new InstalledError(getType(),
                                                                  getName(),
                                                                  getMy(),
                                                                  message));
    }

    @JsonProperty("error")
    public String getError()
    {
        return message;
    }
}
