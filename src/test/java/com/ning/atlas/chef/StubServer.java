package com.ning.atlas.chef;

import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Server;

public class StubServer extends Server
{
    public StubServer(String ip)
    {
        this(ip, ip);
    }

    public StubServer(String ip, Base base)
    {
        super(ip, ip, base);
    }

    public StubServer(String externalIP, String internalIP)
    {
        super(externalIP, internalIP, new Base("base", new Environment("environment")));
    }
}
