package com.ning.atlas;

import com.google.common.collect.Lists;

import java.util.List;

public class InitializedSystemTemplate extends InitializedTemplate
{
    private final List<? extends InitializedTemplate> children;

    public InitializedSystemTemplate(String name, List<? extends InitializedTemplate> children)
    {
        super(name);
        this.children = Lists.newArrayList(children);
    }

    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return children;
    }
}
