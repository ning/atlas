package com.ning.atlas;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SSHBootStrapper implements BootStrapper
{

    private final static Logger logger = LoggerFactory.getLogger(SSHBootStrapper.class);

    private final File   privateKeyFile;
    private final String userName;

    public SSHBootStrapper(File privateKeyFile, String userName)
    {

        this.privateKeyFile = privateKeyFile;
        this.userName = userName;
    }

    public void bootStrap(Server s) throws InterruptedException
    {

        boolean success = false;
        while (!success) {
            try {
                executeRemote(s, s.getBootStrap());
                success = true;
            }
            catch (IOException e) {
                logger.warn("exception trying to bootstrap", e);
                Thread.sleep(1000);
            }
        }
    }

    public String executeRemote(Server server, String command) throws IOException
    {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(server.getExternalIpAddress());

        PKCS8KeyFile keyfile = new PKCS8KeyFile();
        keyfile.init(privateKeyFile);
        ssh.authPublickey(userName, keyfile);

        Session session = ssh.startSession();
        Session.Command c = session.exec(command);
        try {
            return c.getOutputAsString();
        }
        finally {
            c.close();
            session.close();
            ssh.disconnect();
        }
    }
}
