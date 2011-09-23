package com.ning.atlas;

import com.ning.atlas.tree.Tree;

import java.util.Collection;

public interface Node extends Tree
{
    public String getType();
    public String getName();
    public My getMy();

    public Collection<? extends Node> getChildren();

    Identity getId();
}
