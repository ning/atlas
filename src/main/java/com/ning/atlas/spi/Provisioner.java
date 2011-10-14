package com.ning.atlas.spi;

import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.Base;

public interface Provisioner
{
    Server provision(Base base, Node node) throws UnableToProvisionServerException;
}
