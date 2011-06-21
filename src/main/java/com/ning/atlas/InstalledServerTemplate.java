package com.ning.atlas;

public class InstalledServerTemplate extends InstalledTemplate
{
    private final Server server;

    public InstalledServerTemplate(String type, String name, My my, Server installed)
    {
        super(type, name, my);
        this.server = installed;
    }

    public Server getServer()
    {
        return server;
    }
}
