package com.ning.atlas.galaxy;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class GalaxyInstaller extends BaseComponent implements Installer
{
    private final Logger log = LoggerFactory.getLogger(GalaxyInstaller.class);

    private final String sshUser;
    private final String sshKeyFile;

    public GalaxyInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");
    }

//    @Override
    public void install(Server server, String fragment, Node root, Node node) throws Exception
    {
//        log.info("using galaxy to install {} on {}", server, fragment);
//        Iterable<InitializedServer> shells = filter(findInstancesOf(root, InitializedServer.class), new Predicate<InitializedServer>()
//        {
//            @Override
//            public boolean apply(InitializedServer input)
//            {
//                log.info("looking at {}", input.getMy().toJson());
//                return "shell".equals(input.getMy().get("galaxy"));
//            }
//        });
//
//        if (Iterables.isEmpty(shells)) {
//            log.warn("unable to find a :galaxy => 'shell' host to run install on, failing");
//            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
//        }
//
//        InitializedServer shell = Iterables.getFirst(shells, null);
//        assert shell != null;
//
//
//        Iterator<String> top_parts = Splitter.on(':').split(fragment).iterator();
//        String config_path = top_parts.next();
//        String service_type = parts.next();
//        String service_version = parts.next();
//
//        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
//        try {
//            log.debug("installing {} on {}", fragment, server.getInternalAddress());
//
//
//            String[] parts = config_path.split("/");
//            String env = parts[0];
//            String version = parts[1];
//            String type = Joiner.on('/').join(Arrays.asList(parts).subList(2, parts.length));
//
//            String internal_first_part = Splitter.on('.').split(server.getInternalAddress()).iterator().next();
//
//            String query_cmd = format("galaxy -i %s show", internal_first_part);
//            String out;
//            do {
//                out = ssh.exec(query_cmd);
//            }
//            while (out.contains("No agents matching the provided filter"));
//
//            String cmd = format("galaxy -i %s assign %s %s %s", internal_first_part, env, version, type);
//            log.debug("about to run '{}'", cmd);
//
//            String o;
//            do {
//                o = ssh.exec(cmd, 1, TimeUnit.MINUTES);
//            }
//            while (o.contains("SocketError: getaddrinfo: Name or service not known"));
//
//            String cmd2 = format("galaxy -i %s start", internal_first_part);
//            log.debug("about to run '{}'", cmd2);
//            ssh.exec(cmd2, 1, TimeUnit.MINUTES);
//        }
//        catch (Exception e) {
//            log.warn("unable to install galaxy component", e);
//        }
//        finally {
//            ssh.close();
//        }
    }

    @Override
    public Future<String> describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        return Futures.immediateFuture("install <thing> via galaxy");
    }

    @Override
    public Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
