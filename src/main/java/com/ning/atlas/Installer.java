package com.ning.atlas;

public interface Installer
{
    public void install(Server server,
                        String fragment,
                        Node root,
                        Node node) throws Exception;
}
