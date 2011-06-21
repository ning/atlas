package com.ning.atlas;

public interface Server
{
    public String getExternalIpAddress();

    public String getInternalIpAddress();

    Server initialize(ProvisionedTemplate root);

    Base getBase();
}
