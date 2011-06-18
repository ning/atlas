package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

public class InitializedServerTemplate extends InitializedTemplate
{
    @JsonIgnore
    private final Server server;

    public InitializedServerTemplate(String type, String name, My my, Server server)
    {
        super(type, name, my);
        this.server = server;
    }

    @JsonIgnore
    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @JsonIgnore
    public Server getServer()
    {
        return server;
    }

    @JsonProperty("external-ip")
    public String getExternalIP()
    {
        return server.getExternalIpAddress();
    }

    @JsonProperty("internal-ip")
    public String getInternalIP()
    {
        return server.getInternalIpAddress();
    }
}
