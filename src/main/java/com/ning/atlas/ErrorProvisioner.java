package com.ning.atlas;

import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;

import java.util.Map;

public class ErrorProvisioner implements Provisioner
{
    public ErrorProvisioner(Map<String, String> args)
    {

    }

    public ErrorProvisioner()
    {

    }

    public Server provision(Base base, Node node)
    {
        throw new IllegalStateException("No provisioner available!");
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space)
    {
        return "raise an error";
    }
}
