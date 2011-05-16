package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.ning.atlas.tree.Tree;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Template implements Tree<Template>
{
    private final String name;
    private final AtomicInteger cardinality = new AtomicInteger(1);

    public Template(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public int getCardinality()
    {
        return this.cardinality.get();
    }

    public void setCardinality(int count)
    {
        this.cardinality.set(count);
    }

    public final BoundTemplate normalize(Environment env)
    {
        Iterable<BoundTemplate> itty =  normalize(env, new Stack<String>());
        return Iterables.getOnlyElement(itty);
    }

    protected abstract Iterable<BoundTemplate> normalize(Environment env, Stack<String> names);

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", name)
                      .add("cardinality", cardinality.get())
                      .add("children", getChildren())
                      .toString();
    }
}
