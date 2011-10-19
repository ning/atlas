package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;

import java.util.Map;
import java.util.concurrent.Future;

public class ErrorInstaller extends BaseComponent implements Installer
{

    public ErrorInstaller(Map<String, String> attributes)
    {

    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("raise an error");
    }

    @Override
    public Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
