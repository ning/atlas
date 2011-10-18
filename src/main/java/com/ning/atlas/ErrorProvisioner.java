package com.ning.atlas;

import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;

import java.util.Map;
import java.util.concurrent.Future;

public class ErrorProvisioner extends BaseComponent implements Provisioner
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
    public Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space)
    {
        return "raise an error";
    }
}
