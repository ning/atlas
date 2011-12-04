package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;

import java.util.Collections;
import java.util.List;

public class Host extends Element
{
    private final Uri<Provisioner> provisionerUri;
    private final List<Uri<Installer>> inits;
    private final List<Uri<Installer>> installs;

    public Host(Identity id,
                Uri<Provisioner> provisionerUri,
                List<Uri<Installer>> inits,
                List<Uri<Installer>> installs,
                My my)
    {
        super(id, my, Collections.<Element>emptyList());
        this.provisionerUri = provisionerUri;
        this.inits = inits;
        this.installs = installs;
    }

    public List<Uri<Installer>> getInstallationUris()
    {
        return installs;
    }

    public List<Uri<Installer>> getInitializationUris()
    {
        return inits;
    }

    public Uri<Provisioner> getProvisionerUri()
    {
        return provisionerUri;
    }
}
