package com.ning.atlas.spi;

public interface Installer
{
    public void install(Server server,
                        String fragment,
                        Node root,
                        Node node) throws Exception;
}
