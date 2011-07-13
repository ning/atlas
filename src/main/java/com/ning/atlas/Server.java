package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class Server
{

    private final String internalIp;
    private final String externalIp;

    @JsonIgnore
    private final Base base;

    public Server(String internalIp, String externalIp, Base base)
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

    @JsonIgnore
    public Base getBase()
    {
        return base;
    }

    @JsonIgnore
    public Map<String,String> getEnvironmentProperties()
    {
        return getBase().getProperties();
    }
}
