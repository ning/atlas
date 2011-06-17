package com.ning.atlas;

import com.ning.atlas.tree.Tree;

public abstract class InitializedTemplate implements Tree<InitializedTemplate>
{
    private final String type;
    private final String name;

    public InitializedTemplate(String type, String name)
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

    @Override
    public abstract Iterable<? extends InitializedTemplate> getChildren();
}
