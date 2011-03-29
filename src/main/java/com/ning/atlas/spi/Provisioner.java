package com.ning.atlas.spi;

import com.ning.atlas.Server;
import com.ning.atlas.template.SystemManifest;

import java.util.Collection;
import java.util.Set;

public interface Provisioner
{
    Set<Server> provisionBareServers(SystemManifest m) throws InterruptedException;

    void bootStrapServers(Set<Server> servers) throws Exception;

    void destroy(Collection<Server> servers);

    String executeRemote(Server s, String command) throws Exception;
}
