package com.ning.atlas.galaxy;

import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class MicroGalaxyInstaller extends ConcurrentComponent<String>
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
    public String perform(Host host, Uri<? extends Component> uri, Deployment d)
    {
        String fragment = uri.getFragment();
        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();
        SSH ssh = null;
        try {
            ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalAddress());
            log.debug("installing {} on {}", fragment, server.getExternalAddress());
            //
            String cmd = format("echo 'cd ~%s; sudo -u %s ugx stop; sudo -u %s ugx clean; sudo -u %s ugx -b %s deploy; sudo -u %s ugx start' > /tmp/ugx_install",
                                         microGalaxyUser,
                                         microGalaxyUser,
                                         microGalaxyUser,
                                         microGalaxyUser,
                                         fragment,
                                         microGalaxyUser);
            log.warn(cmd);
            ssh.exec(cmd);
            String out = ssh.exec("sh /tmp/ugx_install");
            log.warn(out);
            return out;
        }
        catch (IOException e) {
            log.warn("unable to install {}", fragment, e);
            return e.getMessage();
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
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return null;
    }
}
