package com.ning.atlas.main;

import java.util.concurrent.Callable;

public enum Command
{
    help
        {
            @Override
            public Callable<?> create(MainOptions mo)
            {
                return new HelpCommand(mo);
            }
        },
    ls
        {
            public Callable<?> create(MainOptions mo)
            {
                return new ListCommand();
            }
        },
    init
        {
            public Callable<?> create(MainOptions mo)
            {
                return new InitCommand(mo);
            }
        },
    start
        {
            public Callable<?> create(MainOptions mo)
            {
                return new StartCommand(mo);
            }
        },
    ssh
        {
            public Callable<?> create(MainOptions mo)
            {
                return new SSHCommand(mo);
            }
        };

    public abstract Callable<?> create(MainOptions mo);
}
