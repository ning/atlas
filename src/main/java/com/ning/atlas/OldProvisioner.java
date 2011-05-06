package com.ning.atlas;

import com.ning.atlas.Server;
import com.ning.atlas.template.NormalizedTemplate;

import java.util.Collection;
import java.util.Set;

public interface OldProvisioner
{
    Set<Server> provisionBareServers(NormalizedTemplate m) throws InterruptedException;
    void destroy(Collection<Server> servers);
}
