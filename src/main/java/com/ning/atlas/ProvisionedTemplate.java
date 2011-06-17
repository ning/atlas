package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.tree.Tree;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class ProvisionedTemplate implements Tree<ProvisionedTemplate>
{
    private final String type;
    private final String name;

    public ProvisionedTemplate(String type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public abstract List<? extends ProvisionedTemplate> getChildren();
    public abstract ListenableFuture<? extends InitializedTemplate> initialize(Executor ex, ProvisionedTemplate root);
}
