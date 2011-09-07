package com.ning.atlas;

public interface Installer
{
    public void install(Server server,
                        String fragment,
                        Thing root,
                        Thing node) throws Exception;
}
