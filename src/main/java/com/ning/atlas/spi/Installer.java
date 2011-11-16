package com.ning.atlas.spi;

import com.ning.atlas.Host;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Installer extends Component
{
    public Future<Status> install(final Host server,
                                  final Uri<Installer> uri,
                                  final Deployment deployment);

    public Future<Status> uninstall(final Identity hostId,
                                    final Uri<Installer> uri,
                                    final Deployment deployment);

    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment);
}
