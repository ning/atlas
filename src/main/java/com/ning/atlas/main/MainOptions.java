package com.ning.atlas.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.Collections;

import static java.util.Arrays.asList;

public class MainOptions
{
    private final String       environmentPath;
    private final String       systemPath;
    private final OptionParser parser;
    private final Command      command;
    private final String[]     commandArguments;

    public MainOptions(String... args)
    {
        parser = new OptionParser();
        parser.posixlyCorrect(true);

        OptionSpec<String> e = parser.acceptsAll(asList("e", "env", "environment"), "Environment specification file")
                                     .withRequiredArg()
                                     .ofType(String.class);
        OptionSpec<String> s = parser.acceptsAll(asList("s", "sys", "system"), "System specification file")
                                     .withRequiredArg()
                                     .ofType(String.class);

        OptionSet o = parser.parse(args);

        if ((o.has(e) && o.has(s))) {
            this.environmentPath = o.valueOf(e);
            this.systemPath = o.valueOf(s);
            this.command = Command.valueOf(o.nonOptionArguments().size() >= 1 ? o.nonOptionArguments().get(0) : "help");
            this.commandArguments = o.nonOptionArguments().size() >= 1
                                    ? o.nonOptionArguments()
                                       .subList(1, o.nonOptionArguments().size())
                                       .toArray(new String[o.nonOptionArguments().size() - 1])
                                    : new String[0];
        }

        else {
            System.err.println("Missing one or both of environment or system specification paths");
            this.command = Command.help;
            this.commandArguments = new String[0];
            this.environmentPath = "";
            this.systemPath = "";
        }
    }


    public String getEnvironmentPath()
    {
        return environmentPath;
    }

    public String getSystemPath()
    {
        return systemPath;
    }

    public OptionParser getParser()
    {
        return parser;
    }

    public Command getCommand()
    {
        return command;
    }

    public String[] getCommandArguments()
    {
        return commandArguments;
    }
}
