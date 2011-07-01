package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.ning.atlas.base.Maybe;
import com.sun.istack.internal.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Environment
{
    private final List<Base> bases = new CopyOnWriteArrayList<Base>();

    private final List<Environment>            children     = new CopyOnWriteArrayList<Environment>();
    private final AtomicReference<Provisioner> provisioner  = new AtomicReference<Provisioner>(new ErrorProvisioner());
    private final Map<String, Initializer>     initializers = Maps.newConcurrentMap();
    private final Map<String, Installer>       installers   = Maps.newConcurrentMap();
    private final Map<String, String>          properties   = Maps.newConcurrentMap();

    private final String name;

    private final Maybe<Environment> parent;

    public Environment(String name)
    {
        this(name, new ErrorProvisioner());
    }

    public Environment(String name, Provisioner provisioner)
    {
        this(name, provisioner, Collections.<String, Initializer>emptyMap(), null);
    }

    public Environment(@NotNull String name,
                       @NotNull Provisioner provisioner,
                       @NotNull Map<String, Initializer> initializers,
                       @Nullable Environment parent)
    {
        this.name = name;
        this.parent = Maybe.elideNull(parent);
        this.setProvisioner(provisioner);
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

    public void addInstaller(String name, Installer installer)
    {
        installers.put(name, installer);
    }

    public Maybe<Base> findBase(final String base, final Stack<String> names)
    {
        for (Base candidate : bases) {
            if (candidate.getName().equals(base)) {
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

    public Map<String, Initializer> getInitializers()
    {
        return initializers;
    }

    public Map<String, Installer> getInstallers()
    {
        return installers;
    }

    public List<Environment> getChildren()
    {
        return children;
    }

    public void addProperties(Map<String, String> props)
    {
        this.properties.putAll(props);
    }

    public Map<String, String> getProperties()
    {
        Map<String, String> rs = Maps.newHashMap();
        rs.putAll(parent.to(new Function<Environment, Map<String, String>>()
        {
            @Override
            public Map<String, String> apply(@Nullable Environment input)
            {
                return input.getProperties();
            }
        }).otherwise(Collections.<String, String>emptyMap()));

        // override parent props with ours
        rs.putAll(this.properties);
        return rs;
    }
}
