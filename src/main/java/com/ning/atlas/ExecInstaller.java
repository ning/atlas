package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

public class ExecInstaller extends ConcurrentComponent
{
    private final static Logger log = Logger.get(ExecInstaller.class);

    private final String creds;

    public ExecInstaller(Map<String, String> attributes)
    {
        this.creds = attributes.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        SSH ssh = new SSH(host, d.getSpace(), creds);
        try {
            String out = ssh.exec(uri.getFragment());
            log.info("output of exec is %s", out);
            return out;
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new IllegalStateException("No clearing exec actions");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture(String.format("will execute { %s } remotely", uri.getFragment()));
    }
}
