package com.ning.atlas;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ning.atlas.base.Maybe;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedMultimap;

public class Environment
{
    private final List<Base> bases = new CopyOnWriteArrayList<Base>();

    private final Multimap<String, Map.Entry<String, String>> overrides =
        synchronizedMultimap(ArrayListMultimap.<String, Map.Entry<String, String>>create());

    private final List<Environment> children = new CopyOnWriteArrayList<Environment>();

    private final AtomicReference<Provisioner> provisioner = new AtomicReference<Provisioner>(new ErrorProvisioner());

    private final Map<String, Initializer> initializers = Maps.newConcurrentMap();

    private final String name;

    public Environment(String name)
    {
        this.name = name;
    }

    public Environment(String name, Provisioner provisioner)
    {
        this.name = name;
        this.provisioner.set(provisioner);
    }

    public Environment(String name, Provisioner provisioner, Map<String, Initializer> initializers)
    {
        this(name, provisioner);
        this.initializers.putAll(initializers);
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", name)
                      .add("provisioner", provisioner.get())
                      .add("initializers", initializers)
                      .add("children", children)
                      .add("overrides", overrides)
                      .add("bases", bases)
                      .toString();
    }

    public void addChild(Environment e)
    {
        this.children.add(e);
    }

    public void setProvisioner(Provisioner provisioner)
    {
        this.provisioner.set(provisioner);
    }

    public Provisioner getProvisioner()
    {
        return provisioner.get();
    }

    public void addInitializer(String name, Initializer init)
    {
        initializers.put(name, init);
    }

    public Maybe<Base> findBase(final String base, final Stack<String> names)
    {
        String name = overrideFor(base, "base", names);
        for (Base candidate : bases) {
            if (candidate.getName().equals(name)) {
                return Maybe.definitely(candidate);
            }
        }

        for (Environment child : children) {
            Maybe<Base> rs = child.findBase(base, names);
            if (rs.isKnown()) {
                return rs;
            }
        }

        return Maybe.unknown();
    }

    public void addBase(Base base)
    {
        bases.add(base);
    }


    public String overrideFor(Object defaultValue, String name, Stack<String> names)
    {
        String key = Joiner.on('.').join(names);
        for (Map.Entry<String, String> pair : overrides.get(key)) {
            if (name.equals(pair.getKey())) {
                return pair.getValue();
            }
        }
        return defaultValue.toString();
    }

    public void override(String key, String value)
    {
        List<String> parts = newArrayList(Splitter.on(':').split(key));
        String new_key = parts.get(0);
        this.overrides.put(new_key, Maps.immutableEntry(parts.get(1), value));
    }

    public Map<String, Initializer> getInitializers()
    {
        return initializers;
    }
}
