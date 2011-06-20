package com.ning.atlas;

import com.ning.atlas.tree.Tree;

import java.util.List;

public class InstalledTemplate implements Tree<InstalledTemplate>
{
    private final String type;
    private final String name;
    private final My my;

    public InstalledTemplate(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
    }

    @Override
    public List<? extends InstalledTemplate> getChildren()
    {
        return null;
    }


    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public My getMy()
    {
        return my;
    }
}
