package com.ning.atlas.noop;

import com.google.common.collect.Lists;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Node;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public class NoOpInstaller implements Installer
{
    private Collection<Pair<Identity, Uri<Installer>>> installed = Lists.newArrayList();

    @Override
    public void install(Server server, String fragment, Node root, Node node) throws Exception
    {

    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space)
    {
        return "do nothing with " + uri;
    }

    public Iterable<Pair<Identity,Uri<Installer>>> getInstalled()
    {
        return installed;
    }
}
