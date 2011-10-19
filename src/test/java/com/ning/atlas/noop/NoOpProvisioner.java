package com.ning.atlas.noop;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.Future;

public class NoOpProvisioner extends BaseComponent implements Provisioner
{
    private Collection<Pair<Identity, Uri<Provisioner>>> provisioned = Lists.newArrayList();

    @Override
    public Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        provisioned.add(Pair.of(node.getId(), uri));
        return Futures.immediateFuture(null);
    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("do nothing");
    }

    public Collection<Pair<Identity, Uri<Provisioner>>> getProvisioned()
    {
        return provisioned;
    }
}
