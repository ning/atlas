package com.ning.atlas.noop;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.Space;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;

public class NoOpProvisioner implements Provisioner
{
    @Override
    public Server provision(Base base, Node node) throws UnableToProvisionServerException
    {
        return new Server("1.1.1.1", "2.2.2.2");
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space)
    {
        return "do nothing";
    }
}
