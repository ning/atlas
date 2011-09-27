package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Server;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Collection;
import java.util.Map;

@JsonPropertyOrder({"type", "name", "server"})
public class InstalledServer extends InstalledElement
{
    private final Server server;
    private final Map<String, String> environmentProperties;

    public InstalledServer(Identity id, String type, String name, My my, Server installed, Map<String, String> environmentProperties)
    {
        super(id, type, name, my);
        this.environmentProperties = environmentProperties;
        this.server = installed;
    }

    public Server getServer()
    {
        return server;
    }

    @Override
    @JsonIgnore
    public Collection<? extends InstalledElement> getChildren()
    {
        return super.getChildren();
    }

    @JsonProperty("environment")
    public Map<String, String> getEnvironmentProperties() {
        return environmentProperties;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}
