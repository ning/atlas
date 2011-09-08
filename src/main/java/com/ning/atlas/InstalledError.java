package com.ning.atlas;

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
