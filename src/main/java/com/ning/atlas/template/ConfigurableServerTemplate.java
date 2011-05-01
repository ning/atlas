package com.ning.atlas.template;

import com.google.common.base.Objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurableServerTemplate extends DeployTemplate implements ServerTemplate
{
    private final AtomicReference<String> base = new AtomicReference<String>();
    private final AtomicReference<String> init = new AtomicReference<String>();
    private final List<String> installations = new CopyOnWriteArrayList<String>();

    public ConfigurableServerTemplate(String name)
    {
        super(name);
    }

    public ConfigurableServerTemplate(String name, String base, String bootstrap)
    {
        super(name);
        this.base.set(base);
        this.init.set(bootstrap);
    }

    @Override
    public DeployTemplate addChild(DeployTemplate unit, int count)
    {
        throw new UnsupportedOperationException("May not add children to a server");
    }

    @Override
    public DeployTemplate shallowClone()
    {
        ConfigurableServerTemplate t = new ConfigurableServerTemplate(getName());
        t.setBase(getBase());
        t.addInstallations(getInstallations());
        t.setInit(getInit());
        return t;
    }

    @Override
    public DeployTemplate deepClone()
    {
        // no children on service, same as shallow clone
        return shallowClone();
    }

    @Override
    public UnitType getUnitType()
    {
        return UnitType.Service;
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

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("base", base.get()).toString();
    }
}
