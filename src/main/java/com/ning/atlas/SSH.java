package com.ning.atlas;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class SSH
{
	// http://www.jarvana.com/jarvana/view/net/schmizz/sshj/0.1.1/sshj-0.1.1-javadoc.jar!/net/schmizz/sshj/SSHClient.html

	private final static Logger logger = LoggerFactory.getLogger(SSH.class);
	private final SSHClient ssh;
	private final String host;
	private final int port;
	private final static int TIMEOUT_MINUTES = 2; // time out in minutes

	public enum AuthType {
		AUTH_KEY, AUTH_PASSWORD
	}

	// Key
	public SSH(File privateKeyFile, String userName, String host) throws IOException
	{
		this(privateKeyFile, userName, host, SSHClient.DEFAULT_PORT);
	}

	// Key
	public SSH(File privateKeyFile, String userName, String host, int port) throws IOException
	{
		this(privateKeyFile, userName, null, host, port, AuthType.AUTH_KEY, SSH.TIMEOUT_MINUTES, TimeUnit.MINUTES);
	}

	// Password
	public SSH(String passWord, String userName, String host) throws IOException
	{
		this(passWord, userName, host, SSHClient.DEFAULT_PORT);
	}

	// Password
	public SSH(String passWord, String userName, String host, int port) throws IOException
	{
		this(null, userName, passWord, host, port, AuthType.AUTH_PASSWORD, SSH.TIMEOUT_MINUTES, TimeUnit.MINUTES);

	}

	public SSH(File privateKeyFile, String userName, String passWord, String host, int port, AuthType authtype, long time, TimeUnit unit) throws IOException
	{
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
				ssh.connect(host, port);
				if (authtype == AuthType.AUTH_KEY) {
					PKCS8KeyFile keyfile = new PKCS8KeyFile();
					keyfile.init(privateKeyFile);
					ssh.authPublickey(userName, keyfile);
				} else if (authtype == AuthType.AUTH_PASSWORD) {
					ssh.authPassword(userName, passWord);
				}
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
		this.host = host;
		this.ssh = ssh;
		this.port = port;
	}

	public void close() throws IOException
	{
		ssh.disconnect();
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
        logger.debug("executing {} on {}", command, host);
        Session s = ssh.startSession();
        try {
            logger.debug("executing '{}' on {}", command, host);
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
		logger.debug(format("uploading %s to %s:%d:%s", localFile.getAbsolutePath(), host, port, remotePath));
		ssh.newSCPFileTransfer().upload(localFile.getAbsolutePath(), remotePath);
	}
}
