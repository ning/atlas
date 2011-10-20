package com.ning.atlas.spi;

import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Installer extends Component
{
    public Future<?> install(Host server,
                      Uri<Installer> uri,
                      Space space,
                      SystemMap map);

    public Future<String> describe(Host server,
                            Uri<Installer> uri,
                            Space space,
                            SystemMap map);
}
