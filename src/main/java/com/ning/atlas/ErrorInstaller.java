package com.ning.atlas;

import java.util.Map;

public class ErrorInstaller implements Installer
{

    public ErrorInstaller(Map<String, String> attributes)
    {

    }

    @Override
    public void install(Server server, String arg, Thing root, Thing node)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
