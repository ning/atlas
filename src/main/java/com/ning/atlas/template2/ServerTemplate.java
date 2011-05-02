package com.ning.atlas.template2;

import com.ning.atlas.template.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTemplate extends Template
{
    private final AtomicReference<Base> base          = new AtomicReference<Base>();
    private final List<String>          installations = new CopyOnWriteArrayList<String>();

    public ServerTemplate(String name)
    {
        super(name);
    }

    @Override
    protected final Iterable<Template> normalize(Environment env, Stack<String> names)
    {
        names.push(getName());
        final List<Template> rs = new ArrayList<Template>();
        for (int i = 0; i < env.cardinalityFor(getCardinality(), names); i++) {
            final ServerTemplate dup = new ServerTemplate(getName());
            dup.setCardinality(1); // normalized to cardinality 1
            dup.setBase(env.translateBase(getBase()));
            dup.addInstallations(env.translateInstallations(installations));
            rs.add(dup);
        }
        names.pop();
        return rs;
    }

    public Iterable<? extends Template> getChildren()
    {
        return Collections.emptyList();
    }

    public List<String> getInstallations()
    {
        return installations;
    }

    public void setBase(String base)
    {
        this.base.set(new Base(base));
    }

    public void setBase(Base base)
    {
        this.base.set(base);
    }

    public Base getBase()
    {
        return base.get();
    }

    public void addInstallations(List<String> installations)
    {
        this.installations.addAll(installations);
    }
}
