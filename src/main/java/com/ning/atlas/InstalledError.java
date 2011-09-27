package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;

public class InstalledError extends InstalledElement
{
    private final String message;

    public InstalledError(Identity id, String type, String name, My my, String message)
    {
        super(id, type, name, my);
        this.message = message;
    }

    public InstalledError(Identity id, String type, String name, My my, Exception e)
    {
        this(id, type, name, my, e.getMessage());
    }
}
