package com.ning.atlas.noop;

import com.ning.atlas.Installer;
import com.ning.atlas.Server;
import com.ning.atlas.Thing;

public class NoOpInstaller implements Installer
{
    @Override
    public void install(Server server, String fragment, Thing root, Thing node) throws Exception
    {
    }
}
