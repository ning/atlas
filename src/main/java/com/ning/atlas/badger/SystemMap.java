package com.ning.atlas.badger;

import com.google.common.collect.ImmutableList;

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
}
