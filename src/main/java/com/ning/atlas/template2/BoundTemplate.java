package com.ning.atlas.template2;

import com.ning.atlas.tree.Tree;

public abstract class BoundTemplate implements Tree<BoundTemplate>
{
    private final String name;

    protected BoundTemplate(String name)
    {
        this.name = name;
    }

    public abstract Iterable<? extends BoundTemplate> getChildren();

    public String getName()
    {
        return name;
    }
}
