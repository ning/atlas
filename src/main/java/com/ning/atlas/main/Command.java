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
    update
        {
            public Callable<?> create(MainOptions mo)
            {
                return new UpdateCommand();
            }
        },
    udpate
        {
            public Callable<?> create(MainOptions mo)
            {
                return new UpdateCommand();
            }
        },
    start
        {
            public Callable<?> create(MainOptions mo)
            {
                return new UpdateCommand();
            }
        },
    ssh
        {
            public Callable<?> create(MainOptions mo)
            {
                return new SSHCommand(mo);
            }
        },
    scp
        {
            public Callable<?> create(MainOptions mo)
            {
                return new SCPCommand(mo);
            }
        },
    query
        {
            public Callable<?> create(MainOptions mo)
            {
                return new SpaceQueryCommand(mo);
            }
        },
    destroy
        {
            public Callable<?> create(MainOptions mo)
            {
                return new DestroyCommand(mo);
            }
        };

    public abstract Callable<?> create(MainOptions mo);
}
