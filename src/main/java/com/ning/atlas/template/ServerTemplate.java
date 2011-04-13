package com.ning.atlas.template;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil;

import java.util.List;

public class ServerTemplate extends DeployTemplate
{
    private String base;
    private String bootstrap;
    private final List<String> installations = Lists.newArrayList();

    public ServerTemplate(String name)
    {
        super(name);
    }

    @Override
    public DeployTemplate addChild(DeployTemplate unit, int count)
    {
        throw new UnsupportedOperationException("May not add children to a server");
    }

    @Override
    public DeployTemplate shallowClone()
    {
        ServerTemplate t = new ServerTemplate(getName());
        t.setBase(getBase());
        t.addInstallations(getInstallations());
        t.setBootstrap(getBootstrap());
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
        this.base = base;
    }

    public String getBase()
    {
        return base;
    }

    public void addInstallations(List<String> installations)
    {
        this.installations.addAll(installations);
    }

    public String getBootstrap()
    {
        return bootstrap;
    }

    public void setBootstrap(String bootstrap)
    {
        this.bootstrap = bootstrap;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("base", base).toString();
    }
}
