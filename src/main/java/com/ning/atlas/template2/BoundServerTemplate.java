package com.ning.atlas.template2;

import java.util.Collections;
import java.util.Stack;

public class BoundServerTemplate extends BoundTemplate
{
    private final Base base;

    public BoundServerTemplate(ServerTemplate serverTemplate, Environment env, Stack<String> names)
    {
        super(serverTemplate.getName());
        this.base = env.translateBase(serverTemplate.getBase(), names);
    }

    public Base getBase()
    {
        return base;
    }

    @Override
    public Iterable<? extends BoundTemplate> getChildren()
    {
        return Collections.emptyList();
    }
}
