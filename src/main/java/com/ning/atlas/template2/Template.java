package com.ning.atlas.template2;

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

    public int getCardinality() {
        return this.cardinality.get();
    }

    public void setCardinality(int count) {
        this.cardinality.set(count);
    }

    public final Iterable<BoundTemplate> normalize(Environment env) {
        return normalize(env, new Stack<String>());
    }

    protected  abstract Iterable<BoundTemplate> normalize(Environment env, Stack<String> names);
}
