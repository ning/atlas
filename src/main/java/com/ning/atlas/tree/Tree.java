package com.ning.atlas.tree;

public interface Tree<T extends Tree>
{
    Iterable<? extends T> getChildren();
}
