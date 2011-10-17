package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.Node;
import com.ning.atlas.tree.Trees;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class SystemMap
{
    private final List<NormalizedTemplate> roots;

    public SystemMap() {
        this(Collections.<NormalizedTemplate>emptyList());
    }

    public SystemMap(List<NormalizedTemplate> roots)
    {
        this.roots = ImmutableList.copyOf(roots);
    }

    public static SystemMap emptyMap()
    {
        return new SystemMap();
    }

    public List<NormalizedTemplate> getRoots()
    {
        return roots;
    }

    public Node getSingleRoot()
    {
        return roots.get(0);
    }

    public Set<NormalizedServerTemplate> findLeaves()
    {
        final Set<NormalizedServerTemplate> rs = Sets.newLinkedHashSet();
        for (NormalizedTemplate root : roots) {
            rs.addAll(Trees.findInstancesOf(root, NormalizedServerTemplate.class));
        }
        return rs;
    }
}
