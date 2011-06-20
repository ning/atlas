package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class InitializedErrorTemplate extends InitializedTemplate
{
    private final String message;

    public InitializedErrorTemplate(String type, String name, My my, String message)
    {
        super(type, name, my);
        this.message = message;
    }

    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<InstalledErrorTemplate> install(Executor exec)
    {
        return Futures.immediateFuture(new InstalledErrorTemplate(getType(),  getName(), getMy()));
    }

    @JsonProperty("error")
    public String getError()
    {
        return message;
    }
}
