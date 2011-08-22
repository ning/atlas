package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.tree.Tree;
import com.ning.atlas.upgrade.UpgradePlan;

import java.util.List;
import java.util.concurrent.Executor;

public abstract class BoundTemplate implements Tree<BoundTemplate>
{
    private final String type;
    private final String name;
    private final My     my;

    protected BoundTemplate(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
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

    public abstract List<BoundTemplate> getChildren();

    public abstract ListenableFuture<? extends ProvisionedElement> provision(Executor exec);

    public abstract UpgradePlan upgradeFrom(InstalledTemplate initialState);
}
