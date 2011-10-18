package com.ning.atlas.databases;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.spi.Space;
import com.ning.atlas.Uri;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static java.lang.String.format;

public class MysqlLoaderInstaller implements Installer
{
    private final Logger log = LoggerFactory.getLogger(MysqlLoaderInstaller.class);
    private final String sshUser;
    private final String sshKeyFile;
    private final String sqlUrlTemplate;

    public MysqlLoaderInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");

        checkNotNull(attributes.get("sql_url_template"), "sql_url_template required");
        this.sqlUrlTemplate = attributes.get("sql_url_template");
    }


    @Override
    public void install(Server server, String fragment, Node root, Node node) throws Exception
    {
//        Iterable<InitializedServer> shells = filter(findInstancesOf(root, InitializedServer.class), new Predicate<InitializedServer>()
//        {
//            @Override
//            public boolean apply(InitializedServer input)
//            {
//                log.debug("looking at {}", input.getMy().toJson());
//                return "shell".equals(input.getMy().get("mysql"));
//            }
//        });
//
//        if (Iterables.isEmpty(shells)) {
//            log.warn("unable to find a :databases => 'shell' host to run install on, failing");
//            throw new IllegalStateException("no galaxy shell defined in the deploy tree, unable to continue");
//        }
//
//        InitializedServer shell = Iterables.getFirst(shells, null);
//        assert shell != null;
//
//        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getServer().getExternalAddress());
//        try {
//            log.debug("installing {} on {}", fragment, server.getInternalAddress());
//            final StringTemplate sql_url_t = new StringTemplate(sqlUrlTemplate);
//            Splitter equal_splitter = Splitter.on('=');
//            for (String pair : Splitter.on(';').split(fragment)) {
//                Iterator<String> itty = equal_splitter.split(pair).iterator();
//                sql_url_t.setAttribute(itty.next(), itty.next());
//            }
//            String sql_url = sql_url_t.toString();
//
//            String s3_fetch = String.format("s3cmd get %s do_it.sql", sql_url);
//            log.info(s3_fetch);
//            ssh.exec(s3_fetch);
//
//            String cmd = format("sqlplus %s/%s@\"(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%s))(CONNECT_DATA=(SID=%s)))\" @do_it.sql",
//                                server.getAttributes().get("username"),
//                                server.getAttributes().get("password"),
//                                server.getInternalAddress(),
//                                server.getAttributes().get("port"),
//                                server.getAttributes().get("name"));
//            log.info("about to load sql via [ " +  cmd + " ]");
//            String out = ssh.exec(cmd);
//        }
//        catch (Exception e) {
//            log.warn("failure to load sql", e);
//        }
//        finally {
//            ssh.close();
//        }
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space)
    {
        return "install <stuff> on rds instance";
    }

}
