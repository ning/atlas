package com.ning.atlas.template2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTemplate extends Template
{
    private final AtomicReference<String> base = new AtomicReference<String>();
    private final AtomicReference<String> init = new AtomicReference<String>();
    private final List<String> installations = new CopyOnWriteArrayList<String>();

    public ServerTemplate(String name)
    {
        super(name);
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
        this.base.set(base);
    }

    public String getBase()
    {
        return base.get();
    }

    public void addInstallations(List<String> installations)
    {
        this.installations.addAll(installations);
    }

    public String getInit()
    {
        return init.get();
    }

    public void setInit(String bootstrap)
    {
        this.init.set(bootstrap);
    }

}
