package com.ning.atlas.template2;

import java.util.ArrayList;
import java.util.List;

public class ProvisionedSystemTemplate extends ProvisionedTemplate
{
    private List<? extends ProvisionedTemplate> children;

    public ProvisionedSystemTemplate(String name, List<ProvisionedTemplate> children)
    {
        super(name);
        this.children = new ArrayList<ProvisionedTemplate>(children);
    }

    public List<? extends ProvisionedTemplate> getChildren()
    {
        return children;
    }
}
