package com.ning.atlas;

public class InstalledErrorTemplate extends InstalledTemplate
{
    private final String message;

    public InstalledErrorTemplate(String type, String name, My my, String message)
    {
        super(type, name, my);
        this.message = message;
    }

    public InstalledErrorTemplate(String type, String name, My my, Exception e)
    {
        this(type, name, my, e.getMessage());
    }
}
