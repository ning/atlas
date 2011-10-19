package com.ning.atlas.spi;

public class Server
{
    private String externalAddress;
    private String internalAddress;

    public String getExternalAddress()
    {
        return externalAddress;
    }

    public void setExternalAddress(String externalAddress)
    {
        this.externalAddress = externalAddress;
    }

    public String getInternalAddress()
    {
        return internalAddress;
    }

    public void setInternalAddress(String internalAddress)
    {
        this.internalAddress = internalAddress;
    }
}
