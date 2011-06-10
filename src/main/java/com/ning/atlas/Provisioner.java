package com.ning.atlas;

public interface Provisioner
{
    Server provision(Base base) throws UnableToProvisionServerException;
}
