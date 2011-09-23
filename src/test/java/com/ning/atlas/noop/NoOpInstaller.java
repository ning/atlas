package com.ning.atlas.noop;

import com.ning.atlas.Installer;
import com.ning.atlas.Server;
import com.ning.atlas.Node;

public class NoOpInstaller implements Installer
{
    @Override
    public void install(Server server, String fragment, Node root, Node node) throws Exception
    {
    }
}
