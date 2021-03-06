package com.ning.atlas.components;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

public class ErrorProvisioner extends BaseComponent implements Provisioner
{
    public ErrorProvisioner(Map<String, String> args)
    {

    }

    public ErrorProvisioner()
    {

    }

    @Override
    public Future<Status> provision(Host node, Uri<Provisioner> uri, Deployment deployment)
    {
        return Futures.immediateFuture(Status.fail(uri.getFragment()));
    }

    @Override
    public Future<Status> destroy(Identity hostId, Uri<Provisioner> uri, Deployment deployment)
    {
        return Futures.immediateFuture(Status.fail("no unprovisioning on he failure provisioner"));
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("raise an error");
    }
}
