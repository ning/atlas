package com.ning.atlas.main;

import java.util.HashMap;

/**
 * This is attempt to associate descriptions with individual command names,
 * found through reflection in the Command enum.
 *
 * This is something which should be statically declared.  However, I'm not
 * finding an obvious benefit to creating a HashMap and filling it in
 * statically.  Therefore, I've taken the age-old approach of "get it working
 * now and learn more about the language later" and using a giant switch/case
 * structure instead.
 *
 * @author sfalvo
 */
public class CommandDescriptions {
    public static String descriptionForCommand(String cmdName) {
        HashMap<String,String> hm = new HashMap<String,String>();

        hm.put("help",
                "Displays help on command-line usage and available commands.");
        hm.put("ls",
                "PLEASE FILL ME IN");
        hm.put("init",
                "Initializes the local Atlas configuration to work with your system and environment.");
        hm.put("update",
                "Updates an existing environment, using the current system and environment as its guide.");
        hm.put("start",
                "Starts an environment.");
        hm.put("ssh",
                "SSH to a specific machine, by symbolic name.");
        hm.put("destroy",
                "Terminate an existing environment.");

        String desc = hm.get(cmdName);
        if(desc == null)
            desc = "No description for command "+cmdName+" exists.";

        return desc;
    }
}
