package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;

import java.util.concurrent.Future;

public class WaitForScratchValueInstaller extends ConcurrentComponent
{
    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {

        Maybe<String> result = Maybe.unknown();
        while (!result.isKnown()) {
            Thread.sleep(100);
            result = d.getScratch().get(uri.getFragment());
        }

        return "okay";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        return "okay";
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("nothing, really");
    }
}
