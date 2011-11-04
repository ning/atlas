package com.ning.atlas;

import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class SSH
{
    // http://www.jarvana.com/jarvana/view/net/schmizz/sshj/0.1.1/sshj-0.1.1-javadoc.jar!/net/schmizz/sshj/SSHClient.html

    private final static Logger logger = Logger.get(SSH.class);

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private final SSHClient ssh;

    public SSH(Host host, Space space, String credentialName) throws IOException
    {
        this(SSHCredentials.lookup(space, credentialName)
                           .otherwise(SSHCredentials.defaultCredentials(space))
                           .otherwise(new IllegalStateException("no ssh credentials available for " + host.getId())),
             space.get(host.getId(), Server.class, Missing.RequireAll)
                  .otherwise(new IllegalStateException("no server info available for " + host.getId()))
                  .getExternalAddress());
    }

    public SSH(SSHCredentials creds, String externalAddress) throws IOException
    {
        this(new File(creds.getKeyFilePath()), creds.getUserName(), externalAddress);
    }

    public SSH(File privateKeyFile, String userName, String host) throws IOException
    {
        this(privateKeyFile, userName, host, 30, TimeUnit.SECONDS);
    }

    public SSH(File privateKeyFile, String userName, String host, long time, TimeUnit unit) throws IOException
    {
        logger.debug("connecting to %s with key file %s and user %s", host, privateKeyFile.getAbsolutePath(), userName);
        long give_up_at = System.currentTimeMillis() + unit.toMillis(time);

        boolean connected = false;
        SSHClient ssh = null;
        while (!connected) {
            if (System.currentTimeMillis() > give_up_at) {
                throw new IOException("gave up trying to connect after too long");
            }
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            try {
                ssh.connect(host);

                PKCS8KeyFile keyfile = new PKCS8KeyFile();
                keyfile.init(privateKeyFile);
                ssh.authPublickey(userName, keyfile);

                connected = true;
            }
            catch (Exception e) {
                // ec2 is not ready yet, probably
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        this.ssh = ssh;
    }

    public SSH(Host host, Space space) throws IOException
    {
        this(host, space, SSHCredentials.DEFAULT_CREDENTIAL_NAME);
    }

    public void close() throws IOException
    {
        ssh.disconnect();
        pool.shutdownNow();
    }

    public void forwardLocalPortTo(int localPort, String targetHost, int targetPort) throws IOException
    {
        final LocalPortForwarder local = ssh.newLocalPortForwarder(new InetSocketAddress("localhost", localPort), targetHost, targetPort);
        pool.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    local.listen();
                }
                catch (IOException e) {
                    logger.warn(e, "ioexception on local port forwarded");
                }
            }
        });
    }

    public String exec(String commandFormatString, Object... args) throws IOException
    {
        return exec(format(commandFormatString, args));
    }

    public String exec(String command) throws IOException
    {
        return exec(command, 1, TimeUnit.HOURS);
    }

    public String exec(String command, int time, TimeUnit unit) throws IOException
    {
        Session s = ssh.startSession();
        try {
            Session.Command cmd = s.exec(command);

            StringBuilder all = new StringBuilder();
            BufferedReader out = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            String buf;
            while (null != (buf = out.readLine())) {
                logger.debug(buf);
                all.append(buf).append("\n");
            }

            cmd.join(time, unit);
            cmd.close();
            return all.toString();
        }
        finally {
            s.close();
        }
    }


    public void scpUpload(File localFile, String remotePath) throws IOException
    {
        ssh.newSCPFileTransfer().upload(localFile.getAbsolutePath(), remotePath);
    }
}
