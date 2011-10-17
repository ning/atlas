package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.Space;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;
import com.ning.atlas.Uri;

public interface Provisioner
{
    Server provision(Base base, Node node) throws UnableToProvisionServerException;

    /**
     * Human useful description of what is going to happen here
     */
    String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space);
}
