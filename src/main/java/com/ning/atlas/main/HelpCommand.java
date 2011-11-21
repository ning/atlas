package com.ning.atlas.main;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.Arrays;
import com.ning.atlas.main.Command;
import com.ning.atlas.main.CommandDescriptions;

public class HelpCommand implements Callable<Void>
{
    private final MainOptions mainOptions;

    public HelpCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }


    public Void call()
    {
        try {
            Class<?> enumClass = Command.class;

            mainOptions.getParser().printHelpOn(System.out);
            System.out.format("\nAvailable Commands:\n");
            for(Object member: enumClass.getEnumConstants()) {
                String commandName = member.toString();
                String commandDescription = CommandDescriptions.descriptionForCommand(commandName);
                System.out.format("- %s:\n  %s\n\n", commandName, commandDescription);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
