package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SystemTemplate extends Template
{
    private final List<Template> children;

    public SystemTemplate(String name,
                          Map<String, Object> my,
                          List<?> cardinality,
                          List<Template> children)
    {
        super(name, new My(my), cardinality);
        this.children = ImmutableList.copyOf(children);
    }

    public Collection<? extends Template> getChildren()
    {
        return children;
    }

    public List<Element> _nom(Identity parent)
    {
        List<Element> rs = Lists.newArrayList();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {

            Identity id = parent.createChild(getType(), node_name);

            List<Element> chillins = Lists.newArrayListWithCapacity(getChildren().size());

            for (Template child : getChildren()) {
                Iterable<Element> r2 = child._nom(id);
                Iterables.addAll(chillins, r2);
            }
            Bunch me = new Bunch(id, getMy(), chillins);
            rs.add(me);
        }
        return rs;
    }
}
