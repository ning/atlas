package com.ning.atlas.spi;

import com.ning.atlas.Server;
import com.ning.atlas.template.Manifest;

import java.util.Collection;
import java.util.Set;

public interface Provisioner
{
    Set<Server> provisionServers(Manifest m);

    void destroy(Collection<Server> servers);
}
