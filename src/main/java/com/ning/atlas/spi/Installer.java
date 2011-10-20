package com.ning.atlas.spi;

import com.ning.atlas.Host;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Installer extends Component
{
    public Future<?> install(Host server,
                             Uri<Installer> uri,
                             Deployment deployment);

    public Future<String> describe(Host server,
                                   Uri<Installer> uri,
                                   Deployment deployment);
}
