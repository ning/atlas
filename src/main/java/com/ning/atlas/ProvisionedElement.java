package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class ProvisionedElement implements Thing
{
    private final Identity id;
    private final String type;
    private final String name;
    private final My     my;

    public ProvisionedElement(Identity id, String type, String name, My my)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.my = my;
    }

    public Identity getId()
    {
        return id;
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

    public final ListenableFuture<? extends InitializedTemplate> initialize(ErrorCollector errors, Executor exec)
    {
        return initialize(errors, exec, this);
    }

    protected abstract ListenableFuture<? extends InitializedTemplate> initialize(ErrorCollector errors,
                                                                                  Executor ex,
                                                                                  ProvisionedElement root);
}
