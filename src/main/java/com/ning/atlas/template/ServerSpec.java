package com.ning.atlas.template;

public class ServerSpec
{
    private final String name;
    private final ConfigurableServerTemplate template;

    public ServerSpec(String name, ConfigurableServerTemplate template)
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

    public String getInit()
    {
        return template.getInit();
    }
}
