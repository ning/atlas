package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;
import com.ning.atlas.Uri;

import java.util.concurrent.Future;

public interface Provisioner extends Component
{
    @Deprecated
    Server provision(Base base, Node node) throws UnableToProvisionServerException;


    Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map);

    /**
     * Human useful description of what is going to happen here
     */
    String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space);
}
