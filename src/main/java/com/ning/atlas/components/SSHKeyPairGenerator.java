package com.ning.atlas.components;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.protocols.SSHCredentials;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkState;

public class SSHKeyPairGenerator extends BaseLifecycleListener
{
    private final Map<String, String> userToCredentialName = Maps.newConcurrentMap();

    /**
     * Map<username,credential-identifier>
     */
    public SSHKeyPairGenerator(Map<String, String> config)
    {
        this.userToCredentialName.putAll(config);
    }

    @Override
    public Future<?> startDeployment(Deployment d)
    {
        File storage_root = new File(".atlas/ssh");
        if (!storage_root.exists()) {
            checkState(storage_root.mkdirs(), "unable to create ssh key storage directory!");
        }
        try {
            for (String user_name : userToCredentialName.keySet()) {
                String cred_name = userToCredentialName.get(user_name);
                Maybe<SSHCredentials> m_creds = SSHCredentials.lookup(d.getSpace(), cred_name);
                if (m_creds.isKnown() && new File(m_creds.getValue().getKeyFilePath()).exists()) {
                    checkState(m_creds.getValue().getUserName().equals(user_name),
                               "user name associated with credential %s is %s but we want it to be %s",
                               cred_name, m_creds.getValue().getUserName(), user_name);
                    // we're good to go!
                }
                else {
                    File private_key = new File(storage_root, cred_name);

                    if (!private_key.exists()) {
                        Process p = new ProcessBuilder("ssh-keygen",
                                                       "-b", "1024",
                                                       "-t", "rsa",
                                                       "-m", "pem",
                                                       "-f", private_key.getAbsolutePath()).start();
                        int exit = p.waitFor();
                        if (exit != 0) {
                            System.err.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
                            System.err.println(CharStreams.toString(new InputStreamReader(p.getErrorStream())));
                            throw new UnsupportedOperationException("Error Handling Not Yet Implemented Here!");
                        }
                    }

                    SSHCredentials creds = new SSHCredentials();
                    creds.setKeyFilePath(private_key.getAbsolutePath());
                    creds.setUserName(user_name);
                    SSHCredentials.store(d.getSpace(), creds, cred_name);
                }
            }


        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }


        return super.startDeployment(d);
    }


    //      ssh-keygen [-q] [-b bits] -t type [-N new_passphrase] [-C comment] [-f output_keyfile]

}
