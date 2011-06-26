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
    provision
        {
            @Override
            public Runnable create(MainOptions mo)
            {
                return new ProvisionCommand(mo);
            }
        },
    initialize
        {
            @Override
            public Runnable create(MainOptions mo)
            {
                return new InitializeCommand(mo);
            }
        },

    install
        {
            @Override
            public Runnable create(MainOptions mo)
            {
                return new InstallCommand(mo);
            }
        };

    public abstract Runnable create(MainOptions mo);
}
