package com.ning.atlas.main;

import org.skife.cli.org.iq80.cli.Cli;
import org.skife.cli.org.iq80.cli.Help;

import java.util.concurrent.Callable;

public class Main
{
    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception
    {
        Cli.buildCli("atlas", Callable.class)
           .withCommands(Converge.class,
                         Destroy.class,
                         ListCommand.class,
                         SSHCommand.class,
                         Help.class)
           .withDefaultCommand(Help.class)
           .build()
           .parse(args)
           .call();
    }
}
