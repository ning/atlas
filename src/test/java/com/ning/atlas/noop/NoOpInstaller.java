package com.ning.atlas.noop;

import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.Server;

public class NoOpInstaller implements Installer
{
    @Override
    public void install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
    }
}
