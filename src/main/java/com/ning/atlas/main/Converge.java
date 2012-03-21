package com.ning.atlas.main;

import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Descriptor;
import com.ning.atlas.Environment;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import org.skife.cli.org.iq80.cli.Arguments;
import org.skife.cli.org.iq80.cli.Command;
import org.skife.cli.org.iq80.cli.Option;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "converge")
public class Converge implements Callable<Void>
{
    @Option(name = "--models", configuration = "model")
    public File modelDirectory = new File("model");

    @Option(name = "--space", configuration = "space")
    public File spaceFile = new File(".atlas", "space.db");

    @Arguments
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
        d.converge();

        return null;
    }
}
