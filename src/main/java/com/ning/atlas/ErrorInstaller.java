package com.ning.atlas;

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
}
