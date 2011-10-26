package com.ning.atlas.main;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Environment;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.Template;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Space;

import java.io.File;

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
        Space space = InMemorySpace.newInstance();

        ActualDeployment d = new ActualDeployment(map, env, space);
        d.perform();
    }
}
