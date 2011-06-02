package com.ning.atlas;

import java.util.Collections;

public class InitializedServerTemplate extends InitializedTemplate
{
    private final Server server;

    public InitializedServerTemplate(String name, Server server)
    {
        super(name);
        this.server = server;
    }

    @Override
    public Iterable<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }
}
