package com.ning.atlas.template;

public class ServerSpec
{
    private final String name;
    private final ServerTemplate template;

    public ServerSpec(String name, ServerTemplate template)
    {
        this.name = name;
        this.template = template;
    }

    public String getName()
    {
        return name;
    }

    public String getBase() {
        return template.getBase();
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String getBootStrap()
    {
        return template.getBootstrap();
    }
}
