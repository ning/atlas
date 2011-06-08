package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;

public class InitializedServerTemplate extends InitializedTemplate
{

    @JsonIgnore
    private final Server server;

    public InitializedServerTemplate(String name, Server server)
    {
        super(name);
        this.server = server;
    }

    @JsonIgnore
    @Override
    public Iterable<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @JsonIgnore
    public Server getServer() {
        return server;
    }

    @JsonProperty("external-ip")
    public String getExternalIP() {
        return server.getExternalIpAddress();
    }
}
