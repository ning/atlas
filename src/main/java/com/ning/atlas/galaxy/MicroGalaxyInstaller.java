package com.ning.atlas.galaxy;

import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ning.atlas.spi.protocols.SSHCredentials.defaultCredentials;
import static com.ning.atlas.spi.protocols.SSHCredentials.lookup;
import static java.lang.String.format;

public class MicroGalaxyInstaller extends ConcurrentComponent
{
    private final Logger log = LoggerFactory.getLogger(MicroGalaxyInstaller.class);

    private final String microGalaxyUser;
    private final String credentialName;

    public MicroGalaxyInstaller(Map<String, String> attributes)
    {
        this.microGalaxyUser = attributes.get("ugx_user");
        checkNotNull(microGalaxyUser, "ugx_user attribute required");

        this.credentialName = attributes.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws IOException
    {
        String fragment = uri.getFragment();
        SSH ssh = new SSH(host, d.getSpace(), credentialName);
        try {
            log.debug("installing {} on {}", fragment, host.getId());
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
            ssh.close();
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        log.info("unwinding {} on {}", uri, hostId);
        SSH ssh = new SSH(hostId, credentialName, d.getSpace());
        try {
            String cmd = format("echo 'cd ~%s; sudo -u %s ugx clean' > /tmp/unwind", microGalaxyUser, microGalaxyUser);
            ssh.exec(cmd);
            ssh.exec("sh /tmp/unwind");
            return "okay";
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return null;
    }
}
