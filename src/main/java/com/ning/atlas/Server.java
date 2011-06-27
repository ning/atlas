package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

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
    public String getExternalIpAddress()
    {
        return externalIp;
    }

    @JsonProperty("internal_address")
    public String getInternalIpAddress()
    {
        return internalIp;
    }

    Base getBase()
    {
        return base;
    }
}
