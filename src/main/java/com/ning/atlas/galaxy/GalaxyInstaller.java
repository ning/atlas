package com.ning.atlas.galaxy;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.ning.atlas.InitializedServerTemplate;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
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
    public Server install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
        Iterable<InitializedServerTemplate> shells = filter(findInstancesOf(root, InitializedServerTemplate.class), new Predicate<InitializedServerTemplate>()
        {
            @Override
            public boolean apply(@Nullable InitializedServerTemplate input)
            {
                log.debug("looking at {}", input.getMy().toJson());
                return "shell".equals(input.getMy().get("galaxy"));
            }
        });

        if (Iterables.isEmpty(shells)) {
            log.warn("unable to find a :galaxy => 'shell' host to run install on, failing");
            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
        }

        InitializedServerTemplate shell = Iterables.getFirst(shells, null);
        assert shell != null;

        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
        try {
            log.debug("installing {} on {}", fragment, server.getInternalAddress());


            String[] parts = fragment.split("/");
            String env = parts[0];
            String version = parts[1];
            String type = parts[2];

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

            return server;
        }
        finally {
            ssh.close();
        }
    }
}
