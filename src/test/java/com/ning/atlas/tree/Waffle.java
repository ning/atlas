package com.ning.atlas.tree;

import com.google.common.collect.Lists;
import com.ning.atlas.Thing;

import java.util.Collection;
import java.util.List;

public class Waffle implements Tree
{

    private List<Waffle> children;

    public Waffle(Waffle... children)
    {
        this.children = Lists.newArrayList(children);
    }

    public Collection<? extends Waffle> getChildren()
    {
        return children;
    }
}
