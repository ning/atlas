package com.ning.atlas;

import java.util.Collection;
import java.util.Collections;

public class ProvisionedServerTemplate extends ProvisionedTemplate
{
    private final BoundServerTemplate boundServerTemplate;
    private final Server server;

    public ProvisionedServerTemplate(BoundServerTemplate boundServerTemplate, Server server)
    {
        super(boundServerTemplate.getName());
        this.boundServerTemplate = boundServerTemplate;
        this.server = server;
    }

    public Collection<? extends ProvisionedTemplate> getChildren()
    {
        return Collections.emptySet();
    }

    public Server getServer()
    {
        return server;
    }
}
