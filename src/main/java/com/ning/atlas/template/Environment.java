package com.ning.atlas.template;

import com.google.common.base.Objects;
import com.ning.atlas.template2.Base;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Environment
{
    private final String name;
    private final List<Base> bases = new CopyOnWriteArrayList<Base>();

    public Environment(String name) {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    public Base base(Base base)
    {
        for (Base candidate : bases) {
            if (candidate.getName().equals(base.getName())) {
                return candidate;
            }
        }
        return base;
    }

    public String init(String init)
    {
        return init;
    }

    public List<String> installations(List<String> installations)
    {
        return installations;
    }

    public Base defineBase(Base base)
    {
        bases.add(base);
        return base;
    }
}
