package com.ning.atlas.spi;

import com.ning.atlas.Server;
import com.ning.atlas.template.SystemAssignment;

import java.util.Collection;
import java.util.Set;

public interface Provisioner
{
    Set<Server> provisionBareServers(SystemAssignment m) throws InterruptedException;
    void destroy(Collection<Server> servers);
}
