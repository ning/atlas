package com.ning.atlas.main;

import java.util.concurrent.Callable;

import org.skife.cli.Cli;
import org.skife.cli.Help;

import com.ning.atlas.config.AtlasConfiguration;

public class Main
{
    @SuppressWarnings("unchecked")
    public static void main(String... args) throws Exception
    {
        Cli.buildCli("atlas", Callable.class)
           .withConfiguration(AtlasConfiguration.global())
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
