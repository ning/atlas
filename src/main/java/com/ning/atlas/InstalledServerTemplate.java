package com.ning.atlas;

public class InstalledServerTemplate extends InstalledTemplate
{
    private final Server installed;

    public InstalledServerTemplate(String type, String name, My my, Server installed)
    {
        super(type, name, my);
        this.installed = installed;
    }
}
