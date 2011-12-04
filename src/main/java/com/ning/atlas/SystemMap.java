package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.ning.atlas.tree.Trees;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class SystemMap
{
    private final List<Element> roots;
    private final Environment env;

    public SystemMap() {
        this(Collections.<Element>emptyList(), new Environment());
    }

    public SystemMap(Element... elements) {
        this(asList(elements), new Environment());
    }

    public SystemMap(List<Element> roots, Environment env)
    {
        this.env = env;
        this.roots = ImmutableList.copyOf(roots);
    }

    public static SystemMap emptyMap()
    {
        return new SystemMap();
    }

    public List<Element> getRoots()
    {
        return roots;
    }

    public Set<Host> findLeaves()
    {
        final Set<Host> rs = Sets.newLinkedHashSet();
        for (Element root : roots) {
            rs.addAll(Trees.findInstancesOf(root, Host.class));
        }
        return rs;
    }

    public Element getSingleRoot()
    {
        return roots.get(0);
    }
}
