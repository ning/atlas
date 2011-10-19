package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.tree.Tree;

import java.util.Collection;
import java.util.List;

public class NormalizedTemplate implements Tree
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
    public Collection<? extends NormalizedTemplate> getChildren()
    {
        return children;
    }

    public Identity getId()
    {
        return id;
    }

    public String getType()
    {
        return id.getType();
    }

    public String getName()
    {
        return id.getName();
    }

    public My getMy()
    {
        return my;
    }
}
