package com.ning.atlas;

public interface Provisioner
{
    Server provision(Base base, Node node) throws UnableToProvisionServerException;
}
