package com.ning.atlas;

public interface Provisioner
{
    Server provision(Base base, Thing node) throws UnableToProvisionServerException;
}
