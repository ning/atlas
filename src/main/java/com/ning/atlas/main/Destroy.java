package com.ning.atlas.main;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Descriptor;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.StringUtils;
import org.skife.cli.org.iq80.cli.Arguments;
import org.skife.cli.org.iq80.cli.Command;
import org.skife.cli.org.iq80.cli.Option;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "destroy")
public class Destroy implements Callable<Void>
{

    @Option(name = "--model", title = "model-directory", configuration = "model")
    public File modelDirectory = new File("model");

    @Option(name = "--space", title = "space-database", configuration = "space")
    public File spaceFile = new File(".atlas", "space.db");

    @Option(name = "-e")
    public String environmentName = "dev";

    @Override
    public Void call() throws IOException
    {
        Space space = SQLiteBackedSpace.create(spaceFile);
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor descriptor = Descriptor.empty();
        for (File file : modelDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".rb")) {
                descriptor = descriptor.combine(p.parseDescriptor(file));
            }
        }

        SystemMap map = descriptor.normalize(environmentName);
        Environment env = descriptor.getEnvironment(environmentName);
        ActualDeployment d = env.planDeploymentFor(map, space);

        d.destroy();

        return null;
    }

}