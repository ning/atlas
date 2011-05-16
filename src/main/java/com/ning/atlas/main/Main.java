package com.ning.atlas.main;

public class Main
{
    public static void main(String... args) throws Exception
    {
        MainOptions mo = new MainOptions(args);
        Runnable c = mo.getCommand().create(mo);
        c.run();
    }
}
