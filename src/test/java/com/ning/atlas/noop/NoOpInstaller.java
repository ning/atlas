package com.ning.atlas.noop;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Node;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.Future;

public class NoOpInstaller extends BaseComponent implements Installer
{
    private Collection<Pair<Identity, Uri<Installer>>> installed = Lists.newArrayList();

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("do nothing with " + uri);
    }

    @Override
    public Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        installed.add(Pair.of(server.getId(), uri));
        return Futures.immediateFuture(null);
    }

    public Iterable<Pair<Identity,Uri<Installer>>> getInstalled()
    {
        return installed;
    }
}
