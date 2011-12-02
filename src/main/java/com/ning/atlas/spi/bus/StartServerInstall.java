package com.ning.atlas.spi.bus;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;

public class StartServerInstall extends ServerEvent
{
    private final Uri<Installer> install;

    public StartServerInstall(Identity serverId, Uri<Installer> install)
    {
        super(serverId);
        this.install = install;
    }

    public Uri<Installer> getInstall()
    {
        return install;
    }
}
