package com.ning.atlas;

import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;

import java.util.Map;

public class ErrorInstaller implements Installer
{

    public ErrorInstaller(Map<String, String> attributes)
    {

    }

    @Override
    public void install(Server server, String arg, Node root, Node node)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space)
    {
        return "raise an error";
    }
}
