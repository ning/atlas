package com.ning.atlas.galaxy;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ning.atlas.spi.protocols.SSHCredentials.defaultCredentials;
import static com.ning.atlas.spi.protocols.SSHCredentials.lookup;
import static java.lang.String.format;

public class GalaxyInstaller extends ConcurrentComponent<String>
{
    private final Logger log = LoggerFactory.getLogger(GalaxyInstaller.class);

    private final String credentialName;

    public GalaxyInstaller(Map<String, String> attributes) {
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
        log.info("using galaxy to install {} on {}", host.getId(), uri.toString());

        final SSHCredentials creds = lookup(d.getSpace(), credentialName)
                    .otherwise(defaultCredentials(d.getSpace()))
                    .otherwise(new IllegalStateException("unable to locate any ssh credentials"));


        Identity shell_id = Identity.valueOf(d.getSpace().require("galaxy-shell"));
        Server shell = d.getSpace().get(shell_id, Server.class, Missing.RequireAll).getValue();
        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();
        SSH ssh = new SSH(creds, shell.getExternalAddress());


        final String fragment = uri.getFragment();
        Iterator<String> top_parts = Splitter.on(':').split(fragment).iterator();
        String config_path = top_parts.next();

        try {
            log.debug("installing {} on {}", fragment, server.getInternalAddress());


            String[] parts = config_path.split("/");
            String env = parts[0];
            String version = parts[1];
            String type = Joiner.on('/').join(Arrays.asList(parts).subList(2, parts.length));

            String internal_first_part = Splitter.on('.').split(server.getInternalAddress()).iterator().next();

            String query_cmd = format("galaxy -i %s show", internal_first_part);
            String out;
            do {
                out = ssh.exec(query_cmd);
            }
            while (out.contains("No agents matching the provided filter"));

            String cmd = format("galaxy -i %s assign %s %s %s", internal_first_part, env, version, type);
            log.debug("about to run '{}'", cmd);

            String o;
            do {
                o = ssh.exec(cmd, 1, TimeUnit.MINUTES);
            }
            while (o.contains("SocketError: getaddrinfo: Name or service not known"));

            String cmd2 = format("galaxy -i %s start", internal_first_part);
            log.debug("about to run '{}'", cmd2);
            return ssh.exec(cmd2, 1, TimeUnit.MINUTES);
        }
        catch (Exception e) {
            log.warn("unable to install galaxy component", e);
            return e.getMessage();
        }
        finally {
            ssh.close();
        }
    }
}
