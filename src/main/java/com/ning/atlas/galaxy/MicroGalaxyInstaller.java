package com.ning.atlas.galaxy;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SSH;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Space;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class MicroGalaxyInstaller extends BaseComponent implements Installer
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

//    @Override
    public void install(Server server, String fragment, Node root, Node node)
    {
        SSH ssh = null;
        try {
            ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalAddress());
            log.debug("installing {} on {}", fragment, server.getExternalAddress());
            ssh.exec(format("cd ~%s; sudo -u %s ugx -b %s deploy; sudo -u %s ugx start",
                            microGalaxyUser,
                            microGalaxyUser,
                            fragment,
                            microGalaxyUser));
        }
        catch (IOException e) {
            log.warn("unable to install {}", fragment, e);
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
    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("install <thing> via microgalaxy");
    }

    @Override
    public Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
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
