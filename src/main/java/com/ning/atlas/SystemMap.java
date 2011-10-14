package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.ning.atlas.spi.Node;

import java.util.Collections;
import java.util.List;

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
}
