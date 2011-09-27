package com.ning.atlas.noop;

import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Node;

public class NoOpInstaller implements Installer
{
    @Override
    public void install(Server server, String fragment, Node root, Node node) throws Exception
    {
    }
}
