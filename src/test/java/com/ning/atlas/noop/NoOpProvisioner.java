package com.ning.atlas.noop;

import com.ning.atlas.Base;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.UnableToProvisionServerException;

public class NoOpProvisioner implements Provisioner
{
    @Override
    public Server provision(Base base, Node node) throws UnableToProvisionServerException
    {
        return new Server("1.1.1.1", "2.2.2.2");
    }
}
