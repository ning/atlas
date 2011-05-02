package com.ning.atlas.template2;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ning.atlas.template2.Base;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedMultimap;

public class Environment
{
    private final String name;
    private final List<Base>                                  bases     = new CopyOnWriteArrayList<Base>();
    private final Multimap<String, Map.Entry<String, String>> overrides =
        synchronizedMultimap(ArrayListMultimap.<String, Map.Entry<String, String>>create());

    public Environment(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", name)
                      .toString();
    }

    public Base translateBase(Base base)
    {
        for (Base candidate : bases) {
            if (candidate.getName().equals(base.getName())) {
                return candidate;
            }
        }
        return base;
    }

    public List<String> translateInstallations(List<String> installations)
    {
        return installations;
    }

    public Base defineBase(Base base)
    {
        bases.add(base);
        return base;
    }


    public String overrideFor(Object base, String name, Stack<String> names) {
        String key = Joiner.on('.').join(names);
        for (Map.Entry<String, String> pair : overrides.get(key)) {
            if (name.equals(pair.getKey())) {
                return pair.getValue();
            }
        }
        return base.toString();
    }

    public int cardinalityFor(int count, Stack<String> names)
    {
        return Integer.parseInt(overrideFor(count, "cardinality", names));
    }

    public void override(String key, String value)
    {
        List<String> parts = newArrayList(Splitter.on(':').split(key));
        String new_key = parts.get(0);
        this.overrides.put(new_key, Maps.immutableEntry(parts.get(1), value));
    }
}
