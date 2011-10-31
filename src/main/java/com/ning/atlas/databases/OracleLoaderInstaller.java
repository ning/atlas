package com.ning.atlas.databases;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.antlr.stringtemplate.StringTemplate;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class OracleLoaderInstaller extends ConcurrentComponent<String>
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


//    @Override
//    public void install(Server server, String fragment, Node root, Node node) throws Exception
//    {
//        Iterable<InitializedServer> shells = filter(findInstancesOf(root, InitializedServer.class), new Predicate<InitializedServer>()
//        {
//            @Override
//            public boolean apply(InitializedServer input)
//            {
//                log.debug("looking at {}", input.getMy().toJson());
//                return "shell".equals(input.getMy().get("oracle"));
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
//    }

    @Override
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture("load the <file> into the rds instance");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        String fragment = uri.getFragment();
        String oracle_shell_id = d.getSpace().require("oracle-shell");
        String attrs = d.getSpace().get(host.getId(), "extra-atlas-attributes").getValue();
        Map<String, String> attr = new ObjectMapper().readValue(attrs, Map.class);
        Server shell = d.getSpace().get(Identity.valueOf(oracle_shell_id), Server.class, Missing.RequireAll).getValue();
        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();

        final StringTemplate sql_url_t = new StringTemplate(sqlUrlTemplate);
        final Splitter equal_splitter = Splitter.on('=');
        for (String pair : Splitter.on(';').split(fragment)) {
            Iterator<String> itty = equal_splitter.split(pair).iterator();
            sql_url_t.setAttribute(itty.next(), itty.next());
        }
        String sql_url = sql_url_t.toString();

        if (d.getSpace().get(host.getId(), sql_url).isKnown()) {
            return "Nothing to be done for " + sql_url;
        }

        SSH ssh = new SSH(new File(sshKeyFile), sshUser, shell.getExternalAddress());
        try {
            String s3_fetch = String.format("s3cmd get %s do_it.sql", sql_url);
            log.info(s3_fetch);
            ssh.exec(s3_fetch);

            String cmd = format("sqlplus %s/%s@\"(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%s))(CONNECT_DATA=(SID=%s)))\" @do_it.sql",
                                attr.get("username"),
                                attr.get("password"),
                                server.getInternalAddress(),
                                attr.get("port"),
                                attr.get("name"));
            log.info("about to load sql via [ " + cmd + " ]");

            String out =  ssh.exec(cmd);
            d.getSpace().store(host.getId(), sql_url, new DateTime().toString());
            return out;
        }
        finally {
            ssh.close();
        }
    }
}
