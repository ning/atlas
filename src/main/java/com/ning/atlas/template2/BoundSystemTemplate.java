package com.ning.atlas.template2;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class BoundSystemTemplate extends BoundTemplate
{
    private final List<BoundTemplate> children;

    public BoundSystemTemplate(SystemTemplate systemTemplate, Environment env, Stack<String> names)
    {
        super(systemTemplate.getName());
        List<BoundTemplate> children = new ArrayList<BoundTemplate>();
        for (Template child : systemTemplate.getChildren()) {
            Iterables.addAll(children, child.normalize(env, names));
        }
        this.children = children;
    }

    @Override
    public Iterable<? extends BoundTemplate> getChildren()
    {
        return Collections.unmodifiableList(children);
    }
}
