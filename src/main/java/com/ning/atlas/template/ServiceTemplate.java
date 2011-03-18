package com.ning.atlas.template;

public class ServiceTemplate extends DeployTemplate
{
    public ServiceTemplate(String name)
    {
        super(name);
    }

    @Override
    public DeployTemplate addChild(DeployTemplate unit, int i)
    {
        throw new UnsupportedOperationException("May not add children to a service");
    }

    @Override
    public DeployTemplate shallowClone()
    {
        ServiceTemplate t = new ServiceTemplate(getName());
        for (String required_prop : getRequiredProperties()) {
            t.addRequiredProperty(required_prop);
        }

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
}
