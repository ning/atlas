package com.ning.atlas.main;

import java.util.concurrent.Callable;

public class Main
{
    public static void main(String... args) throws Exception
    {
        MainOptions mo = new MainOptions(args);
        Callable c = mo.getCommand().create(mo);
        c.call();
    }
}
