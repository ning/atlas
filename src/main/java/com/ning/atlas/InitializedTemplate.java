package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Node;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

public abstract class InitializedTemplate implements Node
{
    private final Identity id;
    private final String type;
    private final String name;
    private final My     my;

    public InitializedTemplate(Identity id, String type, String name, My my)
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

    public final String getName()
    {
        return name;
    }

    public final String getType()
    {
        return type;
    }

    @Override
    public abstract Collection<? extends Node> getChildren();

    public final ListenableFuture<InstalledElement> install(ErrorCollector ec, ExecutorService exec)
    {
        return install(ec, exec, this);
    }

    protected abstract ListenableFuture<InstalledElement> install(ErrorCollector ec, ExecutorService exec, InitializedTemplate root);
}
