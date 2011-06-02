package com.ning.atlas;

import java.util.List;

public class InitializedSystemTemplate extends InitializedTemplate
{
    private final List<? extends InitializedTemplate> children;

    public InitializedSystemTemplate(String name, List<? extends InitializedTemplate> children)
    {
        super(name);
        this.children = children;
    }

    @Override
    public Iterable<? extends InitializedTemplate> getChildren()
    {
        return children;
    }
}
