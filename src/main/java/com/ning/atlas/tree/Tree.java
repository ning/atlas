package com.ning.atlas.tree;

import java.util.Collection;

public interface Tree
{
    Collection<? extends Tree> getChildren();
}
