package com.ning.atlas.errors;

public class ErrorCollector
{
    public String error(Throwable e, String format, Object... args)
    {
        return String.format(format, args);
    }

    public String interrupted(InterruptedException e, String format, Object... args)
    {
        return String.format(format, args);
    }
}
