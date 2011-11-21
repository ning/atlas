package com.ning.atlas.main;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class DestroyCommand implements Callable<Void>
{
    private static final Logger logger = Logger.get(UpdateCommand.class);
    private final MainOptions mainOptions;

    public DestroyCommand(MainOptions mainOptions)
    {
        this.mainOptions = mainOptions;
    }

    @Override
    public Void call() throws IOException
    {
        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
        String sys_path = space.get(InitCommand.ID, "system-path")
                               .otherwise(new IllegalStateException("System not initialized"));
        String env_path = space.get(InitCommand.ID, "environment-path")
                               .otherwise(new IllegalStateException("System not initialized"));

        JRubyTemplateParser p = new JRubyTemplateParser();
        SystemMap map = p.parseSystem(new File(sys_path)).normalize();
        Environment env = p.parseEnvironment(new File(env_path));

        ActualDeployment d = new ActualDeployment(map, env, space);
        d.destroy();

        for (Host host : d.getSystemMap().findLeaves()) {
            System.out.println(host.getId() + " :");
            for (Map.Entry<SpaceKey, String> entry : space.getAllFor(host.getId()).entrySet()) {
                System.out.printf("    %s : %s\n",
                                  entry.getKey().getKey(),
                                  StringUtils.abbreviate(entry.getValue(), 80).replaceAll("\n", "\\n"));
            }
        }
        return null;
    }

}