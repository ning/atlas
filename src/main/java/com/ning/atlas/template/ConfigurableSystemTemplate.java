package com.ning.atlas.template;

public class ConfigurableSystemTemplate extends DeployTemplate implements SystemTemplate
{
    public ConfigurableSystemTemplate(String name)
    {
        super(name);
    }

    @Override
    public DeployTemplate shallowClone()
    {
        ConfigurableSystemTemplate t = new ConfigurableSystemTemplate(getName());
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
