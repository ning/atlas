package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Uri;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerTemplate extends Template
{
    private List<Uri<Installer>> installations = new ArrayList<Uri<Installer>>();
    private String base;

    @Deprecated
    public ServerTemplate(String name) {
        this(name, Collections.<String, Object>emptyMap());
    }

    public ServerTemplate(String name, Map<String, Object> my)
    {
        super(name, new My(my));
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

    public Collection<? extends Node> getChildren()
    {
        return Collections.emptyList();
    }

    public List<Uri<Installer>> getInstallations()
    {
        return installations;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public String getBase()
    {
        return base;
    }

    /**
     * called by jruby template parser
     */
    public void setInstall(List<String> installs)
    {
        this.installations.addAll(Lists.transform(installs, new Function<String, Uri<Installer>>()
        {
            @Override
            public Uri<Installer> apply(@Nullable String input)
            {
                return Uri.valueOf(input);
            }
        }));
//        this.installations = new ArrayList<String>(installs);
    }

}
