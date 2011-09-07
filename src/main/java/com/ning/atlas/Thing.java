package com.ning.atlas;

import com.ning.atlas.tree.Tree;

import java.util.Collection;

public interface Thing extends Tree
{
    public String getType();
    public String getName();
    public My getMy();

    public Collection<? extends Thing> getChildren();
}
