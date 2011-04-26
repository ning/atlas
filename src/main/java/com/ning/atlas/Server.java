package com.ning.atlas;

public interface Server
{
    public String getExternalIpAddress();
    public String getInternalIpAddress();
    public String getBootStrap();
    public String getName();
    public String getBase();
}
