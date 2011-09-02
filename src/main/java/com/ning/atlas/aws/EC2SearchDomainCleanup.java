package com.ning.atlas.aws;

import com.google.common.collect.Lists;
import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedElement;
import com.ning.atlas.ProvisionedServer;
import com.ning.atlas.Server;
import com.ning.atlas.tree.Trees;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class EC2SearchDomainCleanup implements Initializer
{

    private final String sshUser;
    private final String sshKeyFile;

    public EC2SearchDomainCleanup(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");
    }

    @Override
    public void initialize(Server server, String arg, ProvisionedElement root, ProvisionedServer node) throws Exception
    {
        List<ProvisionedServer> all_servers = Trees.findInstancesOf(root, ProvisionedServer.class);
        List<String> base_domains = Lists.newArrayList();
        for (ProvisionedServer ps : all_servers) {
            String int_address = ps.getServer().getInternalAddress();
        }
    }
}
