package com.ning.atlas.main;

import java.io.IOException;
import java.util.concurrent.Callable;

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
            mainOptions.getParser().printHelpOn(System.out);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
