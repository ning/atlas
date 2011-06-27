package com.ning.atlas;

public interface Initializer
{
    Server initialize(Server server, String arg, ProvisionedTemplate root) throws Exception;
}
