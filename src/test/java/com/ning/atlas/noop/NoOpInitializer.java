package com.ning.atlas.noop;

import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedServer;
import com.ning.atlas.ProvisionedElement;
import com.ning.atlas.Server;

public class NoOpInitializer implements Initializer
{
    @Override
    public void initialize(Server server, String arg, ProvisionedElement root, ProvisionedServer node) throws Exception
    {
    }
}
