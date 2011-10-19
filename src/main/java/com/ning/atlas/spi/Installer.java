package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;

import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Installer extends Component
{
    public Future<?> install(NormalizedServerTemplate server,
                      Uri<Installer> uri,
                      Space space,
                      SystemMap map);

    public Future<String> describe(NormalizedServerTemplate server,
                            Uri<Installer> uri,
                            Space space,
                            SystemMap map);
}
