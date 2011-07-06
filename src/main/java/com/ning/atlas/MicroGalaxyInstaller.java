package com.ning.atlas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class MicroGalaxyInstaller implements Installer
{
    private final Logger log = LoggerFactory.getLogger(MicroGalaxyInstaller.class);

    private final String sshUser;
    private final String sshKeyFile;
    private final String microGalaxyUser;

    public MicroGalaxyInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");

        this.microGalaxyUser = attributes.get("ugx_user");
        checkNotNull(microGalaxyUser, "ugx_user attribute required");
    }

    @Override
    public Server install(Server server, String fragment)
    {
        SSH ssh = null;
        try {
            ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalIpAddress());
            log.debug("installing {} on {}", fragment, server.getExternalIpAddress());
            ssh.exec(format("cd ~%s; sudo -u %s ugx -b %s deploy; sudo -u %s ugx start",
                            microGalaxyUser,
                            microGalaxyUser,
                            fragment,
                            microGalaxyUser));
        }
        catch (IOException e) {
            log.warn("unable to install {}", fragment, e);
            return server;
        }
        finally {
            if (ssh != null) {
                try {
                    ssh.close();
                }
                catch (IOException e) {
                    log.warn("unable to close ssh connection", e);
                }
            }
        }
        return server;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MicroGalaxyInstaller that = (MicroGalaxyInstaller) o;

        return sshKeyFile.equals(that.sshKeyFile) && sshUser.equals(that.sshUser);

    }

    @Override
    public int hashCode()
    {
        int result = sshUser.hashCode();
        result = 31 * result + sshKeyFile.hashCode();
        return result;
    }
}
