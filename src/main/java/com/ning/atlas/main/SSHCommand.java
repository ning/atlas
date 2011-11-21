package com.ning.atlas.main;

import com.kenai.constantine.platform.Errno;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIXFactory;
import org.jruby.ext.posix.POSIXHandler;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

public class SSHCommand implements Callable<Void>
{
    private static final Logger logger = Logger.get(SSHCommand.class);

    private static final POSIX posix = POSIXFactory.getPOSIX(new POSIXHandler()
    {

        @Override
        public void error(Errno errno, String s)
        {
        }

        @Override
        public void unimplementedError(String s)
        {
        }

        @Override
        public void warn(WARNING_ID warning_id, String s, Object... objects)
        {
        }

        @Override
        public boolean isVerbose()
        {
            return false;
        }

        @Override
        public File getCurrentWorkingDirectory()
        {
            return new File(".");
        }

        @Override
        public String[] getEnv()
        {
            return new String[0];
        }

        @Override
        public InputStream getInputStream()
        {
            return System.in;
        }

        @Override
        public PrintStream getOutputStream()
        {
            return System.out;
        }

        @Override
        public int getPID()
        {
            return 0;
        }

        @Override
        public PrintStream getErrorStream()
        {
            return System.err;
        }

    }, true);

    private final MainOptions mainOptions;

    public SSHCommand(MainOptions mo)
    {
        this.mainOptions = mo;
    }

    @Override
    public Void call() throws Exception
    {
        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
        String looksee = mainOptions.getCommandArguments()[0];
        SSHCredentials creds = SSHCredentials.defaultCredentials(space)
                                             .otherwise(new IllegalStateException("need to use default creds for ssh right now"));

        for (Identity identity : space.findAllIdentities()) {
            Maybe<Server> server = space.get(identity, Server.class);
            if (server.isKnown() && identity.toExternalForm().contains(looksee)) {
                String[] args = new String[]{
                    // -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i %s %s@%s
                    "/usr/bin/ssh", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", creds.getKeyFilePath(), String
                    .format("%s@%s", creds.getUserName(), server.getValue().getExternalAddress())
                };
                posix.execv("/usr/bin/ssh", args);
            }
        }
        System.err.println("Nothing matched '" + looksee + "'");
        return null;
    }
}
