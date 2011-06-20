package com.ning.atlas;

import java.util.List;

public class InstalledSystemTemplate extends InstalledTemplate
{
    private final List<InstalledTemplate> children;

    public InstalledSystemTemplate(String type, String name, My my, List<InstalledTemplate> children)
    {
        super(type, name, my);
        this.children = children;
    }

    @Override
    public List<InstalledTemplate> getChildren() {
        return this.children;
    }
}
