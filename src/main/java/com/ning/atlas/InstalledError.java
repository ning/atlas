package com.ning.atlas;

public class InstalledError extends InstalledElement
{
    private final String message;

    public InstalledError(String type, String name, My my, String message)
    {
        super(type, name, my);
        this.message = message;
    }

    public InstalledError(String type, String name, My my, Exception e)
    {
        this(type, name, my, e.getMessage());
    }
}
