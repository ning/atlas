package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class BoundTemplate implements Node
{
    private final String type;
    private final String name;
    private final My     my;
    private final Identity id;

    protected BoundTemplate(Identity id, String type, String name, My my)
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

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public My getMy()
    {
        return my;
    }

    public abstract Collection<? extends BoundTemplate> getChildren();

    public abstract List<Provision> provision(ErrorCollector collector, ExecutorService exec);

//    public abstract List<Update> upgradeFrom(InstalledElement initialState);
}
