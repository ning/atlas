package com.ning.atlas;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SystemTemplate extends Template
{
    private final List<Template> children = Lists.newArrayList();

    public SystemTemplate(String name)
    {
        super(name);
    }

    @Override
    public List<BoundTemplate> _normalize(Environment env)
    {

        List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {
            BoundSystemTemplate dup = new BoundSystemTemplate(this, node_name, env);
            rs.add(dup);
        }
        return rs;
    }

    public void addChildren(Iterable<? extends Template> normalize)
    {
        Iterables.addAll(children, normalize);
    }

    public void addChild(Template child)
    {
        children.add(child);
    }

    public List<Template> getChildren()
    {
        return children;
    }
}
