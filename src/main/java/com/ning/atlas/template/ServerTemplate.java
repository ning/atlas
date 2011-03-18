package com.ning.atlas.template;

public class ServerTemplate extends DeployTemplate
{
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
