package com.ning.atlas.main;

public enum Command
{
    help
        {
            @Override
            public Runnable create(MainOptions mo)
            {
                return new HelpCommand(mo);
            }
        },
    start
        {
            public Runnable create(MainOptions mo)
            {
                return new StartCommand(mo);
            }
        };

    public abstract Runnable create(MainOptions mo);
}
