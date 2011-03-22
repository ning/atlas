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

    public List<String> validate()
    {
        List<String> problems = new ArrayList<String>();
        for (String key : template.getRequiredProperties()) {
            if (!props.containsKey(key)) {
                problems.add(String.format("required property '%s' missing", key));
            }
        }

        return problems;
    }

    public String getName()
    {
        return name;
    }

    public ServerTemplate getTemplate()
    {
        return template;
    }

    public Map<String, String> getProps()
    {
        return props;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
