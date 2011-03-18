package com.ning.atlas.template;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerTemplate extends DeployTemplate
{
    private final List<String> installations;

    public ServerTemplate(String name, List<String> installations)
    {
        super(name);
        this.installations = installations;
    }

    public static ServerTemplate create(String name, Map<String, String> args) {
        return new ServerTemplate(name, Collections.<String>emptyList());
    }

    @Override
    public DeployTemplate addChild(DeployTemplate unit, int count)
    {
        throw new UnsupportedOperationException("May not add children to a server");
    }

    @Override
    public DeployTemplate shallowClone()
    {
        ServerTemplate t = new ServerTemplate(getName(), getInstallations());
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

    public List<String> getInstallations()
    {
        return installations;
    }
}
