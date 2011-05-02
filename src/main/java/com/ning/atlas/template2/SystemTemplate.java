package com.ning.atlas.template2;

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
    protected List<Template> normalize(Environment env, Stack<String> names)
    {
        names.push(getName());
        List<Template> rs = new ArrayList<Template>();
        for (int i = 0; i < env.cardinalityFor(getCardinality(), names); i++) {
            SystemTemplate dup = new SystemTemplate(getName());
            dup.setCardinality(1);
            for (Template child : children) {
                dup.addChildren(child.normalize(env ,names));
            }
            rs.add(dup);
        }
        names.pop();
        return rs;
    }

    public void addChildren(Iterable<? extends Template> normalize)
    {
        Iterables.addAll(children, normalize);
    }

    public List<? extends Template> getChildren()
    {
        return children;
    }
}
