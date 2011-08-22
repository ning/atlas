package com.ning.atlas;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class Server
{
    private final String internalIp;
    private final String externalIp;

    @JsonIgnore
    private final Base base;

    @JsonCreator
    public Server(@JsonProperty("internal_address") String internalIp,
                  @JsonProperty("external_address")String externalIp,
                  @JsonProperty("base") Base base)
    {
        this.internalIp = internalIp;
        this.externalIp = externalIp;
        this.base = base;
    }

    @JsonProperty("external_address")
    public String getExternalAddress()
    {
        return externalIp;
    }

    @JsonProperty("internal_address")
    public String getInternalAddress()
    {
        return internalIp;
    }

    public Base getBase()
    {
        return base;
    }

    @JsonIgnore
    public Map<String,String> getEnvironmentProperties()
    {
        return getBase().getProperties();
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, true, Object.class);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}
