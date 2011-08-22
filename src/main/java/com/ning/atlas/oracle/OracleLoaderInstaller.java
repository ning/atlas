package com.ning.atlas.oracle;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.ning.atlas.InitializedServer;
import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;

import com.ning.atlas.aws.RDSProvisioner;
import org.antlr.stringtemplate.StringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.ning.atlas.tree.Trees.findInstancesOf;
import static java.lang.String.format;

public class OracleLoaderInstaller implements Installer
{
    private final Logger log = LoggerFactory.getLogger(OracleLoaderInstaller.class);
    private final String sshUser;
    private final String sshKeyFile;
    private final String sqlUrlTemplate;

    public OracleLoaderInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");

        checkNotNull(attributes.get("sql_url_template"), "sql_url_template required");
        this.sqlUrlTemplate = attributes.get("sql_url_template");

    }

    @Override
    public Server install(Server sserver, String fragment, InitializedTemplate root) throws Exception
    {
        RDSProvisioner.RDSServer server = (RDSProvisioner.RDSServer) sserver;
        Iterable<InitializedServer> shells = filter(findInstancesOf(root, InitializedServer.class), new Predicate<InitializedServer>()
        {
            @Override
            public boolean apply(@Nullable InitializedServer input)
            {
                log.debug("looking at {}", input.getMy().toJson());
                return "shell".equals(input.getMy().get("oracle"));
            }
        });

        if (Iterables.isEmpty(shells)) {
            log.warn("unable to find a :galaxy => 'shell' host to run install on, failing");
            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
        }

        InitializedServer shell = Iterables.getFirst(shells, null);
        assert shell != null;

        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
        try {
            log.debug("installing {} on {}", fragment, server.getInternalAddress());
            final StringTemplate sql_url_t = new StringTemplate(sqlUrlTemplate);
            Splitter equal_splitter = Splitter.on('=');
            for (String pair : Splitter.on(';').split(fragment)) {
                Iterator<String> itty = equal_splitter.split(pair).iterator();
                sql_url_t.setAttribute(itty.next(), itty.next());
            }
            String sql_url = sql_url_t.toString();

            ssh.exec("s3cmd get %s do_it.sql", sql_url);

            String out = ssh.exec(format("sqlplus %s/%s@\"(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%d))(CONNECT_DATA=(SID=%s))) @do_it.sql",
                                         server.getUsername(),
                                         server.getPassword(),
                                         server.getInternalAddress(),
                                         server.getPort(),
                                         server.getName()));
            log.debug(out);

            return server;
        }
        finally {
            ssh.close();
        }

    }


}
