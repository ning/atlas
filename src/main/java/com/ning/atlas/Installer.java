package com.ning.atlas;

public interface Installer
{
    public Server install(Server server, String fragment, InitializedTemplate root) throws Exception;
}
