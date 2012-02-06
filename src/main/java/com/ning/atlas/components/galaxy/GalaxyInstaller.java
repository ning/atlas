package com.ning.atlas.components.galaxy;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ning.atlas.spi.protocols.SSHCredentials.defaultCredentials;
import static com.ning.atlas.spi.protocols.SSHCredentials.lookup;
import static java.lang.String.format;

public class GalaxyInstaller extends ConcurrentComponent
{

    private final Logger log = Logger.get(GalaxyInstaller.class);

    private final String credentialName;

    public GalaxyInstaller(Map<String, String> attributes)
    {
        this.credentialName = attributes.get("credentials");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("install <thing> via galaxy");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws IOException
    {
        log.info("using galaxy to install %s on %s", host.getId(), uri.toString());

        final SSHCredentials creds = lookup(d.getSpace(), credentialName)
            .otherwise(defaultCredentials(d.getSpace()))
            .otherwise(new IllegalStateException("unable to locate any ssh credentials"));


        Identity shell_id = Identity.valueOf(d.getScratch().get("galaxy-shell").otherwise(new IllegalStateException("no galaxy-shell available")));
        Server shell = d.getSpace().get(shell_id, Server.class, Missing.RequireAll).getValue();
        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();
        SSH ssh = new SSH(creds, shell.getExternalAddress());
        try {

            final String fragment = uri.getFragment();

            String internal_hostname = Splitter.on('.').split(server.getInternalAddress()).iterator().next();

            Maybe<String> old_install_fragment = d.getSpace().get(host.getId().createChild("galaxy", "installer"),
                                                                  "current-installation");
            if (old_install_fragment.isKnown()) {
                if (old_install_fragment.getValue().equals(fragment)) {
                    // nothing to do, move along
                    return "leaving current installation in place.";
                }
                else {
                    // need to clear what was there first
                    ssh.exec("galaxy -i %s clear", internal_hostname);
                    log.info("cleared old installation %s on %s", old_install_fragment, host.getId().toExternalForm());
                }
            }
            // new install

            Iterator<String> top_parts = Splitter.on(':').split(fragment).iterator();
            String config_path = top_parts.next();

            log.info("installing %s on %s", fragment, server.getInternalAddress());


            String[] parts = config_path.split("/");
            String env = parts[0];
            String version = parts[1];
            String type = Joiner.on('/').join(Arrays.asList(parts).subList(2, parts.length));

            String query_cmd = format("galaxy -i %s show", internal_hostname);
            String out;
            do {
                out = ssh.exec(query_cmd);
            }
            while (out.contains("No agents matching the provided filter"));

            String cmd = format("galaxy -i %s assign %s %s %s", internal_hostname, env, version, type);
            log.debug("about to run '%s'", cmd);

            String o;
            do {
                o = ssh.exec(cmd, 1, TimeUnit.MINUTES);
            }
            while (o.contains("SocketError: getaddrinfo: Name or service not known"));

            String cmd2 = format("galaxy -i %s start", internal_hostname);
            log.debug("about to run '{}'", cmd2);
            ssh.exec(cmd2, 1, TimeUnit.MINUTES);
            d.getSpace().store(host.getId().createChild("galaxy", "installer"),
                               "current-installation",
                               uri.getFragment());
            log.info("installed %s on %s", uri.getFragment(), host.getId().toExternalForm());
            return "installed";
        }
        catch (Exception e) {
            log.warn(e, "unable to install galaxy component");
            return e.getMessage();
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        log.info("clearing galaxy on %s", hostId);

        final SSHCredentials creds = lookup(d.getSpace(), credentialName)
            .otherwise(defaultCredentials(d.getSpace()))
            .otherwise(new IllegalStateException("unable to locate any ssh credentials"));


        Identity shell_id = Identity.valueOf(d.getScratch().get("galaxy-shell").otherwise(new IllegalStateException("no galaxy-shell available")));
        Server shell = d.getSpace().get(shell_id, Server.class, Missing.RequireAll).getValue();
        Server server = d.getSpace().get(hostId, Server.class, Missing.RequireAll).getValue();
        SSH ssh = new SSH(creds, shell.getExternalAddress());
        try {
            String internal_hostname = Splitter.on('.').split(server.getInternalAddress()).iterator().next();

            String cmd2 = format("galaxy -i %s clear", internal_hostname);
            log.debug("about to run '{}'", cmd2);
            ssh.exec(cmd2, 1, TimeUnit.MINUTES);
            d.getSpace().delete(hostId.createChild("galaxy", "installer"), "current-installation");
            return "unwound";
        }
        catch (Exception e) {
            log.warn(e, "unable to uninstall galaxy component");
            return e.getMessage();
        }
        finally {
            ssh.close();
        }
    }
}
