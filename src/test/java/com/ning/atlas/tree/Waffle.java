package com.ning.atlas.tree;

import com.google.common.collect.Lists;

import java.util.List;

public class Waffle implements Tree<Waffle>
{

    private List<Waffle> children;

    public Waffle(Waffle... children)
    {
        this.children = Lists.newArrayList(children);
    }

    public Iterable<? extends Waffle> getChildren()
    {
        return children;
    }
}
