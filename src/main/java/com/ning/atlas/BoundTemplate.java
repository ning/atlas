package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.tree.Tree;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class BoundTemplate implements Tree<BoundTemplate>
{
    private final String name;

    protected BoundTemplate(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public abstract List<BoundTemplate> getChildren();
    public abstract ListenableFuture<? extends ProvisionedTemplate> provision(Executor exec);
}
