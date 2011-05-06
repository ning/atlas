package com.ning.atlas;

import com.ning.atlas.template2.Base;

public interface Server
{
    public String getExternalIpAddress();
    public String getInternalIpAddress();
    public Base getBase();
}
