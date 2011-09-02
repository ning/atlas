package com.ning.atlas;

import java.util.Map;

public class NoOpInitializer implements Initializer
{

    public NoOpInitializer(Map<String, String> a)
    {

    }

    @Override
    public void initialize(Server server, String arg, ProvisionedElement root, ProvisionedServer node)
    {
    }
}
