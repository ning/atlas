package com.ning.atlas;

public class InitializedServerTemplate extends InitializedTemplate
{
    private final Server server;

    public InitializedServerTemplate(String name, Server server)
    {
        this.server = server;
    }
}
