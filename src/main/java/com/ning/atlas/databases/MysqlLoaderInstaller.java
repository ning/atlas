package com.ning.atlas.databases;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class MysqlLoaderInstaller extends BaseComponent implements Installer
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
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("install <stuff> on rds instance");
    }

    @Override
    public Future<Status> install(Host server, Uri<Installer> uri, Deployment deployment)
    {
        return Futures.immediateFuture(Status.fail(uri.getFragment()));
    }

    @Override
    public Future<Status> uninstall(Identity hostId, Uri<Installer> uri, Deployment deployment)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

}
