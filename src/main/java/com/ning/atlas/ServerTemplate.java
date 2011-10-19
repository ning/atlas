package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerTemplate extends Template
{
    private final List<Uri<Installer>> installations = new ArrayList<Uri<Installer>>();
    private final String base;

    public ServerTemplate(String name,
                          String base,
                          List<?> cardinality,
                          List<Uri<Installer>> installers,
                          Map<String, Object> my)
    {
        super(name, new My(my), cardinality);
        this.installations.addAll(installers);
        this.base = base;
    }

    @Override
    protected List<NormalizedTemplate> _nom(Identity parent)
    {
        final List<NormalizedTemplate> rs = new ArrayList<NormalizedTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {

            Identity id = parent.createChild(getType(), node_name);
            rs.add(new NormalizedServerTemplate(id, getBase(), getMy(), getInstallations()));
        }
        return rs;
    }

    public Collection<? extends NormalizedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    public List<Uri<Installer>> getInstallations()
    {
        return installations;
    }

    public String getBase()
    {
        return base;
    }
}
