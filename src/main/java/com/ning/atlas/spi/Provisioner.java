package com.ning.atlas.spi;

import com.ning.atlas.Base;
import com.ning.atlas.UnableToProvisionServerException;

public interface Provisioner
{
    Server provision(Base base, Node node) throws UnableToProvisionServerException;
}
