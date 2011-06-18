package com.ning.atlas;

import com.ning.atlas.tree.Tree;

import java.util.List;

public abstract class InitializedTemplate implements Tree<InitializedTemplate>
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
    public abstract List<? extends InitializedTemplate> getChildren();
}
