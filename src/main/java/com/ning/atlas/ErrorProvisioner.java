package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
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

    @Override
    public Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("raise an error");
    }
}
