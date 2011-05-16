package com.ning.atlas.main;

import java.io.IOException;

public class HelpCommand implements Runnable
{
    private final MainOptions mainOptions;

    public HelpCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }


    public void run()
    {
        try {
            mainOptions.getParser().printHelpOn(System.out);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
