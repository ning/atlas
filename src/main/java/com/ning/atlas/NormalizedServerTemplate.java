package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Uri;

import java.util.Collections;
import java.util.List;

public class NormalizedServerTemplate extends NormalizedTemplate
{
    private final String base;
    private final List<Uri<Installer>> installations;

    public NormalizedServerTemplate(Identity id,
                                    String base,
                                    My my,
                                    List<Uri<Installer>> installations)
    {
        super(id, my, Collections.<NormalizedTemplate>emptyList());
        this.base = base;
        this.installations = installations;
    }

    public String getBase()
    {
        return base;
    }

    public List<Uri<Installer>> getInstallations()
    {
        return installations;
    }
}
