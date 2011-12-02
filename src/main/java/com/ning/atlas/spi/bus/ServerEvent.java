package com.ning.atlas.spi.bus;

import com.ning.atlas.spi.Identity;

public abstract class ServerEvent
{
    private final Identity serverId;

    public ServerEvent(Identity serverId) {

        this.serverId = serverId;
    }

    public Identity getServerId()
    {
        return serverId;
    }
}
