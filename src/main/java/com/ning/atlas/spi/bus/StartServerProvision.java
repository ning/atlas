package com.ning.atlas.spi.bus;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;

public class StartServerProvision extends ServerEvent
{
    private final Uri<Provisioner> uri;

    public StartServerProvision(Identity serverId, Uri<Provisioner> uri)
    {
        super(serverId);
        this.uri = uri;
    }

    public Uri<Provisioner> getUri()
    {
        return uri;
    }
}
