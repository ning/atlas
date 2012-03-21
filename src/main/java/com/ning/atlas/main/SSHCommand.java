package com.ning.atlas.main;

import com.kenai.constantine.platform.Errno;
import com.ning.atlas.logging.Logger;
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


    public SSHCommand()
    {

    }

    @Override
    public Void call() throws Exception
    {
//        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));
//
//        String looksee = mainOptions.getCommandArguments()[0];
//        Maybe<String[]> remote_commands;
//        if (mainOptions.getCommandArguments().length > 1) {
//            execute a remote command rather than shell in
//            remote_commands = Maybe.definitely(Arrays.copyOfRange(mainOptions.getCommandArguments(),
//                                                                  1, mainOptions.getCommandArguments().length));
//        }
//        else {
//            remote_commands = Maybe.unknown();
//        }
//
//        SSHCredentials creds = SSHCredentials.defaultCredentials(space)
//                                             .otherwise(new IllegalStateException("need to use default creds for ssh right now"));
//
//        for (Identity identity : space.findAllIdentities()) {
//            Maybe<Server> server = space.get(identity, Server.class);
//            if (server.isKnown() && identity.toExternalForm().contains(looksee)) {
//                String[] args = new String[]{
//                    -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i %s %s@%s
//                    "/usr/bin/ssh", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", creds.getKeyFilePath(), String
//                    .format("%s@%s", creds.getUserName(), server.getValue().getExternalAddress())
//                };
//                if (remote_commands.isKnown()) {
//                    String[] new_args = new String[args.length + remote_commands.getValue().length];
//                    System.arraycopy(args, 0, new_args, 0, args.length);
//                    System.arraycopy(remote_commands.getValue(), 0,
//                                     new_args, args.length, remote_commands.getValue().length);
//                    args = new_args;
//                }
//                posix.execv("/usr/bin/ssh", args);
//            }
//        }
//        System.err.println("Nothing matched '" + looksee + "'");
//        return null;
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
