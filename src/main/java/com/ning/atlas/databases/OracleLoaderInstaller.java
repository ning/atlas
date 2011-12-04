package com.ning.atlas.databases;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class OracleLoaderInstaller extends ConcurrentComponent
{
    private static final Logger log = LoggerFactory.getLogger(OracleLoaderInstaller.class);
    private final String sqlUrlTemplate;
    private final String credentialName;

    public OracleLoaderInstaller(Map<String, String> attributes)
    {
        checkNotNull(attributes.get("sql_url_template"), "sql_url_template required");
        this.sqlUrlTemplate = attributes.get("sql_url_template");
        this.credentialName = attributes.get("credentials");
    }

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
        final ST sql_url_t = new ST(sqlUrlTemplate);
        final Splitter equal_splitter = Splitter.on('=');
        for (String pair : Splitter.on(';').split(fragment)) {
            Iterator<String> itty = equal_splitter.split(pair).iterator();
            sql_url_t.add(itty.next(), itty.next());
        }
        String sql_url = sql_url_t.toString();

        if (d.getSpace().get(host.getId(), sql_url).isKnown()) {
            return "already installed";
        }

        SSHCredentials creds = SSHCredentials.lookup(d.getSpace(), credentialName)
            .otherwise(SSHCredentials.defaultCredentials(d.getSpace()))
            .otherwise(new IllegalStateException("unable to find ssh credentials"));


        String oracle_shell_id = d.getSpace().require("oracle-shell");
        String attrs = d.getSpace().get(host.getId(), "extra-atlas-attributes").getValue();
        Map<String, String> attr = new ObjectMapper().readValue(attrs, Map.class);
        Server shell = d.getSpace().get(Identity.valueOf(oracle_shell_id), Server.class, Missing.RequireAll).getValue();
        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();




        if (d.getSpace().get(host.getId(), sql_url).isKnown()) {
            return "Nothing to be done for " + sql_url;
        }

        SSH ssh = new SSH(creds, shell.getExternalAddress());
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

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new IllegalStateException("unable to unwind oracle load");
    }
}
