package com.ning.atlas.spi;

import com.ning.atlas.Server;
import com.ning.atlas.template.SystemManifest;

import java.util.Collection;
import java.util.Set;

public interface Provisioner
{
    Set<Server> provisionServers(SystemManifest m) throws InterruptedException;

    void destroy(Collection<Server> servers);
}
