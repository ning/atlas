package com.ning.atlas;

public interface Server
{
    public String getExternalIpAddress();

    public String getInternalIpAddress();

    Base getBase();
}
