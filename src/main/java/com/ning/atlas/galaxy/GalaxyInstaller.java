package com.ning.atlas.galaxy;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.ning.atlas.InitializedServer;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.ning.atlas.tree.Trees.findInstancesOf;
import static java.lang.String.format;

public class GalaxyInstaller implements Installer
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

    @Override
    public void install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
        Iterable<InitializedServer> shells = filter(findInstancesOf(root, InitializedServer.class), new Predicate<InitializedServer>()
        {
            @Override
            public boolean apply(@Nullable InitializedServer input)
            {
                log.debug("looking at {}", input.getMy().toJson());
                return "shell".equals(input.getMy().get("galaxy"));
            }
        });

        if (Iterables.isEmpty(shells)) {
            log.warn("unable to find a :galaxy => 'shell' host to run install on, failing");
            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
        }

        InitializedServer shell = Iterables.getFirst(shells, null);
        assert shell != null;


        Iterator<String> top_parts = Splitter.on(':').split(fragment).iterator();
        String config_path = top_parts.next();
//        String service_type = parts.next();
//        String service_version = parts.next();

        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
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
            log.debug(ssh.exec(cmd));
            String cmd2 = format("galaxy -i %s start", internal_first_part);
            log.debug("about to run '{}'", cmd2);
            log.debug(ssh.exec(cmd2));
        }
        catch (Exception e) {
            log.warn("unable to install galaxy component", e);
        }
        finally {
            ssh.close();
        }
    }
}
