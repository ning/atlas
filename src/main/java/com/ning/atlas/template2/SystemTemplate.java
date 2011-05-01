package com.ning.atlas.template2;

import com.google.common.collect.Lists;

import java.util.List;

public class SystemTemplate extends Template
{
    private final List<Template> children = Lists.newArrayList();

    public SystemTemplate(String name)
    {
        super(name);
    }

    public List<? extends Template> getChildren()
    {
        return null;
    }
}
