package com.ning.atlas.spi;

import org.codehaus.jackson.annotate.JsonProperty;

public class Server
{
    private String externalAddress;
    private String internalAddress;

    public Server() { }

    public Server(String externalAddress, String internalAddress) {
        this.externalAddress = externalAddress;
        this.internalAddress = internalAddress;
    }

    public Server(String address) {
        this(address, address);
    }

    @JsonProperty("external_address")
    public String getExternalAddress()
    {
        return externalAddress;
    }

    public void setExternalAddress(String externalAddress)
    {
        this.externalAddress = externalAddress;
    }

    @JsonProperty("internal_address")
    public String getInternalAddress()
    {
        return internalAddress;
    }

    public void setInternalAddress(String internalAddress)
    {
        this.internalAddress = internalAddress;
    }
}
