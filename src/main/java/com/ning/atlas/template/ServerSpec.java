package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerSpec
{
    private final String name;
    private final ServerTemplate template;
    private final Map<String, String> props;

    public ServerSpec(String name, ServerTemplate template, Map<String, String> props)
    {
        this.name = name;
        this.template = template;
        this.props = props;
    }

    public String getName()
    {
        return name;
    }

    public String getImage() {
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
