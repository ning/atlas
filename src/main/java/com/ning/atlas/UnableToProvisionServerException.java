package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;

public class UnableToProvisionServerException extends Exception
{
    private final Identity id;
    private final String type;
    private final String name;
    private final My my;

    public UnableToProvisionServerException(String message)
    {
        this(null, null, null, null, message);
    }

    public UnableToProvisionServerException(Identity id, String type, String name, My my, String message)
    {
        super(message);
        this.id = id;
        this.type = type;
        this.name = name;
        this.my = my;
    }

    public Identity getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public My getMy()
    {
        return my;
    }
}
