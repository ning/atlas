package com.ning.atlas;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SystemTemplate extends Template
{
    private final List<Template> children = Lists.newArrayList();

    public SystemTemplate(String name)
    {
        super(name);
    }

    @Override
    public List<BoundTemplate> _normalize(Environment env, Identity parent)
    {
        List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {

            Identity id = parent.createChild(getType(), node_name);

            List<BoundTemplate> chillins = Lists.newArrayListWithCapacity(getChildren().size());

            for (Template child : getChildren()) {
                Iterable<BoundTemplate> r2 = child._normalize(env, id);
                Iterables.addAll(chillins, r2);
            }

            BoundSystemTemplate dup = new BoundSystemTemplate(id, getType(), node_name, getMy(), chillins);

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

    public Collection<? extends Template> getChildren()
    {
        return children;
    }
}
