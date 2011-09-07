package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class ProvisionedElement implements Thing
{
    private final String type;
    private final String name;
    private final My     my;

    public ProvisionedElement(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
    }

    public My getMy()
    {
        return my;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public abstract List<? extends ProvisionedElement> getChildren();

    public ListenableFuture<? extends InitializedTemplate> initialize(Executor exec)
    {
        return initialize(exec, this);
    }

    protected abstract ListenableFuture<? extends InitializedTemplate> initialize(Executor ex, ProvisionedElement root);
}
