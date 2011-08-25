package com.ning.atlas;

public interface Initializer
{
    Server initialize(Server server,
                      String arg,
                      ProvisionedElement root,
                      ProvisionedServer node) throws Exception;
}
