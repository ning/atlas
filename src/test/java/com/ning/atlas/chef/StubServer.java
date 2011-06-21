package com.ning.atlas.chef;

import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Server;

public class StubServer implements Server
{
    private final String externalIP;
    private final String internalIP;
    private final Base base;

    public StubServer(String ip)
    {
        this(ip, ip);
    }


    public StubServer(String externalIP, String internalIP)
    {
        this(externalIP, new Base("base", new Environment("environment")));
    }

    public StubServer(String externalIP, Base base)
    {
        this.externalIP = externalIP;
        this.internalIP = externalIP;
        this.base = base;
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
    public Base getBase()
    {
        return base;
    }
}
