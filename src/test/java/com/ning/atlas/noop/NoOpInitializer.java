package com.ning.atlas.noop;

import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedServerTemplate;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Server;

public class NoOpInitializer implements Initializer
{
    @Override
    public Server initialize(Server server, String arg, ProvisionedTemplate root, ProvisionedServerTemplate node) throws Exception
    {
        return server;
    }
}
