package com.ning.atlas;

import com.ning.atlas.tree.Tree;

public abstract class InitializedTemplate implements Tree<InitializedTemplate>
{
    private final String name;

    public InitializedTemplate(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public abstract Iterable<? extends InitializedTemplate> getChildren();
}
