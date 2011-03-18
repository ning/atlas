package com.ning.atlas.template;

import java.util.Map;

public class SystemTemplate extends DeployTemplate
{
    public SystemTemplate(String name)
    {
        super(name);
    }

    public static SystemTemplate create(String name, Map<String, String> args) {
        return new SystemTemplate(name);
    }

    @Override
    public DeployTemplate shallowClone()
    {
        SystemTemplate t = new SystemTemplate(getName());
        for (String required_prop : getRequiredProperties()) {
            t.addRequiredProperty(required_prop);
        }

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
