package com.ning.atlas.noop;

import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.Server;

public class NoOpInstaller implements Installer
{
    @Override
    public Server install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
        return server;
    }
}
