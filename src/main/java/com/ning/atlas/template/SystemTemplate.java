package com.ning.atlas.template;

public class SystemTemplate extends DeployTemplate
{
    public SystemTemplate(String name)
    {
        super(name);
    }

    @Override
    public DeployTemplate shallowClone()
    {
        SystemTemplate t = new SystemTemplate(getName());
        return t;
    }

    @Override
    public DeployTemplate deepClone()
    {
        DeployTemplate me = shallowClone();
        for (DeployTemplate.SizedChild child : getChildren()) {
            me.addChild(child.getTemplate().deepClone(), child.getCardinality());
        }
        return me;
    }

    @Override
    public UnitType getUnitType()
    {
        return UnitType.System;
    }
}
