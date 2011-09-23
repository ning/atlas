package com.ning.atlas;

import java.util.Map;

public class ErrorProvisioner implements Provisioner
{
    public ErrorProvisioner(Map<String, String> args)
    {

    }

    public ErrorProvisioner()
    {

    }

    public Server provision(Base base, Node node)
    {
        throw new IllegalStateException("No provisioner available!");
    }
}
