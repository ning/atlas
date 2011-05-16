package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProvisionedServerTemplate extends ProvisionedTemplate
{
    @JsonIgnore
    private final BoundServerTemplate boundServerTemplate;

    @JsonIgnore
    private final Server server;

    public ProvisionedServerTemplate(BoundServerTemplate boundServerTemplate, Server server)
    {
        super(boundServerTemplate.getName());
        this.boundServerTemplate = boundServerTemplate;
        this.server = server;
    }

    @JsonIgnore
    public List<? extends ProvisionedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @JsonIgnore
    public Server getServer()
    {
        return server;
    }

    public String getExternalIP() {
        return server.getExternalIpAddress();
    }

    public String getInternalIP() {
        return server.getInternalIpAddress();
    }
}
