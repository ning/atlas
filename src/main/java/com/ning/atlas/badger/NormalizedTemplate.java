package com.ning.atlas.badger;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Node;
import com.ning.atlas.tree.Tree;

import java.util.Collection;
import java.util.List;

public class NormalizedTemplate implements Tree, Node
{
    private final Identity id;
    private final My my;
    private final List<NormalizedTemplate> children;

    public NormalizedTemplate(Identity id, My my, List<NormalizedTemplate> children)
    {
        this.id = id;
        this.my = my;
        this.children = children;
    }

    @Override
    public Collection<? extends Node> getChildren()
    {
        return children;
    }

    public Identity getId()
    {
        return id;
    }

    @Override
    public String getType()
    {
        return id.getType();
    }

    @Override
    public String getName()
    {
        return id.getName();
    }

    public My getMy()
    {
        return my;
    }
}
