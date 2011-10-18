package com.ning.atlas.spi;

import com.ning.atlas.SystemMap;

public interface Component
{
    public void start(SystemMap map, Space space);
    public void finish(SystemMap map, Space space);
}

