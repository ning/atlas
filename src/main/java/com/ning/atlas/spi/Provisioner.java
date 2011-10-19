package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;
import com.ning.atlas.Uri;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * TODO merge with Installer on Component
 */
public interface Provisioner extends Component
{
    public Future<?> provision(NormalizedServerTemplate node,
                               Uri<Provisioner> uri,
                               Space space,
                               SystemMap map);

    public Future<String> describe(NormalizedServerTemplate server,
                                   Uri<Provisioner> uri,
                                   Space space,
                                   SystemMap map);
}
