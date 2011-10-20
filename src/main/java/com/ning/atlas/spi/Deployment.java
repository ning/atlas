package com.ning.atlas.spi;

import com.ning.atlas.Environment;
import com.ning.atlas.SystemMap;

public interface Deployment
{
    public SystemMap getSystemMap();
    public Space getSpace();
    public Environment getEnvironment();
}
