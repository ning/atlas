package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.concurrent.Executor;

public abstract class InitializedTemplate implements Thing
{
    private final String type;
    private final String name;
    private final My     my;

    public InitializedTemplate(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
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
    public abstract Collection<? extends Thing> getChildren();

    public final ListenableFuture<? extends InstalledElement> install(Executor exec)
    {
        return install(exec, this);
    }

    public abstract ListenableFuture<? extends InstalledElement> install(Executor exec, InitializedTemplate root);
}
