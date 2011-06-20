package com.ning.atlas.chef;

import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Server;

public class StubServer implements Server
{
    private final String externalIP;
    private final String internalIP;

    public StubServer(String ip)
    {
        this(ip, ip);
    }


    public StubServer(String externalIP, String internalIP)
    {
        this.externalIP = externalIP;
        this.internalIP = internalIP;
    }

    @Override
    public String getExternalIpAddress()
    {
        return externalIP;
    }

    @Override
    public String getInternalIpAddress()
    {
        return internalIP;
    }

    @Override
    public Server initialize(ProvisionedTemplate root)
    {
        return this;
    }

    @Override
    public Server install()
    {
        return this;
    }
}
