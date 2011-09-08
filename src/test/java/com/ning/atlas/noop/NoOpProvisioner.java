package com.ning.atlas.noop;

import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import com.ning.atlas.Thing;
import com.ning.atlas.UnableToProvisionServerException;

public class NoOpProvisioner implements Provisioner
{
    @Override
    public Server provision(Base base, Thing node) throws UnableToProvisionServerException
    {
        return new Server("1.1.1.1", "2.2.2.2");
    }
}
