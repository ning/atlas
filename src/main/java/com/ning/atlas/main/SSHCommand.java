package com.ning.atlas.main;

import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.Missing;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;

import java.io.File;
import java.util.concurrent.Callable;

public class SSHCommand implements Callable<Void>
{
    private static final Logger logger = Logger.get(SSHCommand.class);

    private final MainOptions mainOptions;

    public SSHCommand(MainOptions mo)
    {
        this.mainOptions = mo;
    }

    @Override
    public Void call() throws Exception
    {
        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
        String sys_path = space.get(InitCommand.ID, "system-path")
                               .otherwise(new IllegalStateException("System not initialized"));
        String env_path = space.get(InitCommand.ID, "environment-path")
                               .otherwise(new IllegalStateException("System not initialized"));

        JRubyTemplateParser p = new JRubyTemplateParser();
        SystemMap map = p.parseSystem(new File(sys_path)).normalize();
        Environment env = p.parseEnvironment(new File(env_path));

        String looksee = mainOptions.getCommandArguments()[0];

        for (Host host : map.findLeaves()) {
            if (host.getId().toExternalForm().contains(looksee)) {
                // match!
                Server s = space.get(host.getId(), Server.class, Missing.RequireAll)
                                .otherwise(new IllegalStateException("no server info available for " + host.getId()));
                SSHCredentials creds = SSHCredentials.defaultCredentials(space)
                                                     .otherwise(new IllegalStateException("need to use default creds for ssh right now"));


                System.out.printf("ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i %s %s@%s\n",
                                  creds.getKeyFilePath(),
                                  creds.getUserName(),
                                  s.getExternalAddress());
                return null;
            }
        }
        return null;
    }
}
