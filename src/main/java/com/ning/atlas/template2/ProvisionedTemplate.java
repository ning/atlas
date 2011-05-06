package com.ning.atlas.template2;

import com.ning.atlas.tree.Tree;

public abstract class ProvisionedTemplate implements Tree<ProvisionedTemplate>
{
    private final String name;

    public ProvisionedTemplate(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
