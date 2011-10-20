package com.ning.atlas.spi;

import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Provisioner extends Component
{
    public Future<?> provision(Host node,
                               Uri<Provisioner> uri,
                               Space space,
                               SystemMap map);

    public Future<String> describe(Host server,
                                   Uri<Provisioner> uri,
                                   Space space,
                                   SystemMap map);
}
