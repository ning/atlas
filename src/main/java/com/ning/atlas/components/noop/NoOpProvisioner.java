package com.ning.atlas.components.noop;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.Future;

public class NoOpProvisioner extends BaseComponent implements Provisioner
{
    private static Collection<Pair<Identity, Uri<Provisioner>>> provisioned = Lists.newArrayList();

    @Override
    public Future<Status> provision(Host node, Uri<Provisioner> uri, Deployment deployment)
    {
        provisioned.add(Pair.of(node.getId(), uri));
        return Futures.immediateFuture(Status.okay());
    }

    @Override
    public Future<Status> destroy(Identity hostId, Uri<Provisioner> uri, Deployment deployment)
    {
        return Futures.immediateFuture(Status.okay());
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("do nothing with " + uri.toString());
    }

    public static Collection<Pair<Identity, Uri<Provisioner>>> getProvisioned()
    {
        return provisioned;
    }

    public static void reset() {
        provisioned.clear();
    }
}
