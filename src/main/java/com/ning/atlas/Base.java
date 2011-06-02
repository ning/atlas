package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Base
{
    private final String name;
    private final Map<String, String> attributes = Maps.newConcurrentMap();
    private final Provisioner provisioner;
    private final Initializer initalizer;

    public Base(String name, Environment env, Map<String, String> attributes)
    {
        this.name = name;
        this.provisioner = env.getProvisioner();
        this.initalizer= env.getInitializer();
        this.attributes.putAll(attributes);
    }

    public Base(String name, Environment env)
    {
        this(name, env, Collections.<String, String>emptyMap());
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public String getName()
    {
        return name;
    }

    public Provisioner getProvisioner()
    {
        return provisioner;
    }

    public Initializer getInitalizer()
    {
        return initalizer;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Base)) return false;

        Base base = (Base) o;

        return attributes.equals(base.attributes) && name.equals(base.name);

    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + attributes.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", getName())
                      .add("attributes", attributes)
                      .toString();
    }
}
