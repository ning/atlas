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
import org.skife.cli.org.iq80.cli.OptionType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkState;

@Command(name = "converge")
public class Converge implements Callable<Void>
{
    @Option(name = "--model", title = "model-directory", configuration = "model")
    public File modelDirectory = new File("model");

    @Option(name = "-e", type = OptionType.GLOBAL)
    public String environmentName = "dev";

    @Override
    public Void call() throws IOException
    {
        File atlas_dir = new File(".atlas");
        File env_dir = new File(atlas_dir, environmentName);
        if (!env_dir.exists()) {
            checkState(env_dir.mkdirs(), "unable to create environment data directory");
        }

        Space space = SQLiteBackedSpace.create(new File(env_dir, "space.db"));
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor descriptor = Descriptor.empty();
        for (File file : modelDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".rb")) {
                Descriptor d =p.parseDescriptor(file);
                descriptor = descriptor.combine(d);
            }
        }

        SystemMap map = descriptor.normalize(environmentName);
        Environment env = descriptor.getEnvironment(environmentName);
        ActualDeployment d = env.planDeploymentFor(map, space);

        d.getScratch().put("atlas.environment-directory", env_dir.getAbsolutePath());

        d.converge();

        return null;
    }
}
