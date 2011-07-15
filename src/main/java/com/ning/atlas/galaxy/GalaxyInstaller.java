package com.ning.atlas.galaxy;

import com.google.common.base.Predicate;
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
                return "shell".equals(input.getMy().get("galaxy"));
            }
        });

        if (Iterables.isEmpty(shells)) {
            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
        }

        InitializedServerTemplate shell = Iterables.getFirst(shells, null);
        assert shell != null;

        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
        log.debug("installing {} on {}", fragment, server.getInternalAddress());


        String[] parts = fragment.split(":");
        String env = parts[0];
        String version = parts[1];
        String type = parts[2];
        ssh.exec("galaxy -i %s assign %s %s %s", server.getInternalAddress(), env, version, type);
        ssh.exec("galaxy -i %s start", server.getInternalAddress());

        return server;
    }
}
