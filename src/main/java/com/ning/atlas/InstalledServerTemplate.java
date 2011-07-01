package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"type", "name", "server"})
public class InstalledServerTemplate extends InstalledTemplate
{
    private final Server server;

    public InstalledServerTemplate(String type, String name, My my, Server installed)
    {
        super(type, name, my);
        this.server = installed;
    }

    public Server getServer()
    {
        return server;
    }

    @Override
    @JsonIgnore
    public List<? extends InstalledTemplate> getChildren()
    {
        return super.getChildren();
    }

    @JsonProperty("environment")
    public Map<String, String> getEnvironmentProperties() {
        return this.server.getEnvironmentProperties();
    }
}
