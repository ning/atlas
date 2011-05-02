package com.ning.atlas.template2;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ning.atlas.template.Environment;

import java.util.ArrayList;
import java.util.List;

public class SystemTemplate extends Template
{
    private final List<Template> children = Lists.newArrayList();

    public SystemTemplate(String name)
    {
        super(name);
    }

    @Override
    protected List<Template> normalize(Environment env)
    {
        List<Template> rs = new ArrayList<Template>();
        for (int i = 0; i < getCount(); i++) {
            SystemTemplate dup = new SystemTemplate(getName());
            dup.setCount(1);
            for (Template child : children) {
                dup.addChildren(child.normalize(env));
            }
            rs.add(dup);
        }
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
