package com.ning.atlas;

public class ErrorProvisioner implements Provisioner
{
    public Server provision(Base base)
    {
        throw new IllegalStateException("No provisioner available!");
    }
}
