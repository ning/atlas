package com.ning.atlas.main;

import com.ning.atlas.Descriptor;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.StringUtils;
import org.skife.cli.org.iq80.cli.Arguments;
import org.skife.cli.org.iq80.cli.Command;
import org.skife.cli.org.iq80.cli.Option;
import org.skife.cli.org.iq80.cli.OptionType;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkState;

@Command(name = "ls")
public class ListCommand implements Callable<Void>
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
                descriptor = descriptor.combine(p.parseDescriptor(file));
            }
        }

        SystemMap map = descriptor.normalize(environmentName);
        for (Host host : map.findLeaves()) {
            System.out.println(host.getId() + " :");
            for (Map.Entry<SpaceKey, String> entry : space.getAllFor(host.getId()).entrySet()) {
                System.out.printf("    %s : %s\n",
                                  entry.getKey().getKey(),
                                  StringUtils.abbreviate(entry.getValue(), 80).replaceAll("\n", "\\n"));
            }
        }
        return null;
    }

//    @Override
//    public Void call() throws Exception
//    {
//        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
//        String sys_path = space.get(InitCommand.ID, "system-path")
//                               .otherwise(new IllegalStateException("System not initialized"));
//        String env_path = space.get(InitCommand.ID, "environment-path")
//                               .otherwise(new IllegalStateException("System not initialized"));
//
//        JRubyTemplateParser p = new JRubyTemplateParser();
//        Environment e = p.parseEnvironment(new File(env_path));
//        SystemMap map = p.parseSystem(new File(sys_path)).normalize(e);
//
//        for (Host host : map.findLeaves()) {
//            System.out.println(host.getId() + " :");
//            for (Map.Entry<SpaceKey, String> entry : space.getAllFor(host.getId()).entrySet()) {
//                System.out.printf("    %s : %s\n",
//                                  entry.getKey().getKey(),
//                                  StringUtils.abbreviate(entry.getValue(), 80).replaceAll("\n", "\\n"));
//            }
//        }
//        return null;
//    }
}
