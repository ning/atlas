package com.ning.atlas.spi;

import com.ning.atlas.Host;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Provisioner extends Component
{
    public Future<Status> provision(Host node,
                                    Uri<Provisioner> uri,
                                    Deployment deployment);

    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment);
}
