package com.ning.atlas.main;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import jnr.ffi.Library;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.IntByReference;

import org.skife.cli.Arguments;
import org.skife.cli.Command;
import org.skife.cli.Option;
import org.skife.cli.OptionType;

import com.ning.atlas.Descriptor;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.space.Space;

@Command(name = "ssh")
public class SSHCommand implements Callable<Void>
{

    private static final LibC posix = Library.loadLibrary("c", LibC.class);

    @Option(name = "--model", title = "model-directory", configuration = "model")
    public File modelDirectory = new File("model");

    @Option(name = "-e", type = OptionType.GLOBAL)
    public String environmentName = "dev";

    @Arguments
    public String query;

    @Override
    public Void call() throws IOException
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor descriptor = Descriptor.empty();
        for (File file : modelDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".rb")) {
                descriptor = descriptor.combine(p.parseDescriptor(file));
            }
        }
        File atlas_dir = new File(".atlas");
        File env_dir = new File(atlas_dir, environmentName);
        if (!env_dir.exists()) {
            checkState(env_dir.mkdirs(), "unable to create environment data directory");
        }

        Space space = SQLiteBackedSpace.create(new File(env_dir, "space.db"));
        SystemMap map = descriptor.normalize(environmentName);
        for (Host host : map.findLeaves()) {
            Maybe<Server> server = space.get(host.getId(), Server.class);
            if (host.getId().toExternalForm().contains(query) && server.isKnown()) {
                IntByReference pid = new IntByReference();


                SSHCredentials creds = SSHCredentials.defaultCredentials(space)
                                                     .otherwise(new IllegalStateException("need to use default creds for ssh right now"));

                String[] argv = new String[]{
                    // -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i %s %s@%s
                    "ssh", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", creds.getKeyFilePath(), String
                    .format("%s@%s", creds.getUserName(), server.getValue().getExternalAddress())
                };
                posix.posix_spawnp(pid, "ssh", null, null, argv, getEnv());
                posix.waitpid(pid.intValue(), 0, 0);
                return null;
            }
        }
        return null;
    }

    public static interface LibC
    {
        int posix_spawnp(@Out IntByReference pid,
                         @In CharSequence path,
                         @In Pointer fileActions,
                         @In Pointer attr,
                         @In CharSequence[] argv,
                         @In CharSequence[] envp);

        int waitpid(int a, int b, int c);

    }

    public static String[] getEnv()
    {
        String[] envp = new String[System.getenv().size()];
        int i = 0;
        for (Map.Entry<String, String> pair : System.getenv().entrySet()) {
            envp[i++] = new StringBuilder(pair.getKey()).append("=").append(pair.getValue()).toString();
        }
        return envp;
    }
}
