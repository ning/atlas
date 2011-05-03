package com.ning.atlas.template2;

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
    protected final Iterable<BoundTemplate> normalize(Environment env, Stack<String> names)
    {
        names.push(getName());
        final List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        for (int i = 0; i < env.cardinalityFor(getCardinality(), names); i++) {

            rs.add(new BoundServerTemplate(this, env, names));
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
