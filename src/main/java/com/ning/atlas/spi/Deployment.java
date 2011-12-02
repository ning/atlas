package com.ning.atlas.spi;

import com.ning.atlas.Environment;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.bus.NotificationBus;
import com.ning.atlas.spi.space.Space;

public interface Deployment
{
    public SystemMap getSystemMap();
    public Space getSpace();
    public Environment getEnvironment();
    public NotificationBus getEventBus();
}
