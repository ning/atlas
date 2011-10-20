package com.ning.atlas.noop;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.Installer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.Future;

public class NoOpInstaller extends BaseComponent implements Installer
{
    private static Collection<Pair<Identity, Uri<Installer>>> installed = Lists.newArrayList();

    @Override
    public Future<String> describe(Host server, Uri<Installer> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("do nothing with " + uri);
    }

    @Override
    public Future<?> install(Host server, Uri<Installer> uri, Space space, SystemMap map)
    {
        installed.add(Pair.of(server.getId(), uri));
        return Futures.immediateFuture(null);
    }

    public static Iterable<Pair<Identity,Uri<Installer>>> getInstalled()
    {
        return installed;
    }

    public static void reset() {
        installed.clear();
    }
}
