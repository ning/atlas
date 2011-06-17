package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ning.atlas.tree.Tree;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Template implements Tree<Template>
{
    private final String type;
    private final List<String> cardinality = new CopyOnWriteArrayList<String>(new String[] {"0"});

    public Template(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public List<String> getCardinality()
    {
        return this.cardinality;
    }

    public void setCardinality(int count)
    {
        List<String> names = Lists.newArrayListWithExpectedSize(count);
        for (int i = 0; i < count; i++) {
            names.add(String.valueOf(i));
        }
        setCardinality(names);
    }

    public void setCardinality(List<String> names)
    {
        this.cardinality.clear();
        this.cardinality.addAll(names);
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
                      .add("type", type)
                      .add("cardinality", cardinality)
                      .add("children", getChildren())
                      .toString();
    }
}
