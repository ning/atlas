package com.ning.atlas.main;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.SpaceKey;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class StartCommand implements Runnable
{
    private static final Logger logger = Logger.get(StartCommand.class);
    private final MainOptions mainOptions;

    public StartCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    @Override
    public void run()
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        SystemMap map = p.parseSystem(new File(mainOptions.getSystemPath())).normalize();
        Environment env = p.parseEnvironment(new File(mainOptions.getEnvironmentPath()));
        Space space = null;
        try {
            space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
        }
        catch (IOException e) {
            logger.warn(e, "unable to create storage for space");
            throw new IllegalStateException(e);
        }

        ActualDeployment d = new ActualDeployment(map, env, space);
        d.perform();

        for (Host host : d.getSystemMap().findLeaves()) {
            System.out.println(host.getId() + " :");
            for (Map.Entry<SpaceKey, String> entry : space.getAllFor(host.getId()).entrySet()) {
                System.out.printf("    %s : %s\n",
                                  entry.getKey().getKey(),
                                  StringUtils.abbreviate(entry.getValue(), 80).replaceAll("\n", "\\n"));
            }
        }

    }
}
