package com.ning.atlas;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.MoreExecutors.getExitingExecutorService;

public class SSHInitializer implements Initializer
{
    private final static Logger logger = LoggerFactory.getLogger(SSHInitializer.class);

    private final File            privateKeyFile;
    private final String          userName;
    private final ExecutorService exec;

    public SSHInitializer(File privateKeyFile, String userName)
    {
        this.exec = getExitingExecutorService(new ThreadPoolExecutor(1, 100, 100, TimeUnit.MILLISECONDS,
                                                                     new ArrayBlockingQueue<Runnable>(100)),
                                              100, TimeUnit.MILLISECONDS);
        this.privateKeyFile = privateKeyFile;
        this.userName = userName;
    }

    public ListenableFuture<Server> initialize(final Server s)
    {
        ListenableFutureTask<Server> rs = new ListenableFutureTask<Server>(new Callable<Server>()
        {
            @Override
            public Server call() throws Exception
            {
                boolean success = false;
                while (!success) {
                    try {
                        executeRemote(s, "s.getBootStrap()");
                        success = true;
                    }
                    catch (ConnectException e) {
                        // sshd not running yet
                        Thread.sleep(1000);
                    }
                    catch (TransportException e) {
                        // these happen sometimes when sshd is accepting cons but not yet ready
                        Thread.sleep(1000);
                    }
                    catch (UserAuthException e) {
                        // for some reason on EC2 the key isn't available initially. NFC why.
                        Thread.sleep(1000);
                    }
                    catch (IOException e) {
                        logger.warn("exception trying to bootstrap", e);
                        Thread.sleep(1000);
                    }
                }
                return s;
            }
        });
        this.exec.submit(rs);
        return rs;
    }

    public String executeRemote(Server server, String command) throws IOException
    {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(server.getExternalIpAddress());

        PKCS8KeyFile keyfile = new PKCS8KeyFile();
        keyfile.init(privateKeyFile);
        ssh.authPublickey(userName, keyfile);

        if (command.contains("\n")) {
            File tmp = File.createTempFile("bootstrap", "tmp");
            Files.append(command, tmp, Charset.forName("UTF8"));

            ssh.newSCPFileTransfer().upload(tmp.getAbsolutePath(), "/tmp/");

            Session set_exec = ssh.startSession();
            Session.Command c = set_exec.exec("chmod +x /tmp/" + tmp.getName());
            c.join();
            set_exec.close();

            Session exec = ssh.startSession();
            Session.Command c2 = exec.exec("/tmp/" + tmp.getName());

            String rs = c2.getOutputAsString();
            exec.close();
            ssh.disconnect();
            tmp.delete();
            return rs;

        }
        else {
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
}
