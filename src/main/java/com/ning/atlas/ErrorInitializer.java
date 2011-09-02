package com.ning.atlas;

import java.util.Map;

public class ErrorInitializer implements Initializer
{

    public ErrorInitializer(Map<String, String> attributes)
    {

    }

    @Override
    public void initialize(Server server, String arg, ProvisionedElement root,
                           ProvisionedServer node)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
