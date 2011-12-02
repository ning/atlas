package com.ning.atlas.spi.bus;

import com.google.common.collect.ImmutableList;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;

import java.util.List;

public class FinishedServerInstallSequence extends ServerEvent
{
    private final List<Uri<Installer>> installs;

    public FinishedServerInstallSequence(Identity serverId, List<Uri<Installer>> installs)
    {
        super(serverId);
        this.installs = ImmutableList.copyOf(installs);
    }

    public List<Uri<Installer>> getInstalls()
    {
        return installs;
    }
}
