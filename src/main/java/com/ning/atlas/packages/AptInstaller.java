package com.ning.atlas.packages;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

public class AptInstaller extends ConcurrentComponent
{

    private static final Logger log = Logger.get(AptInstaller.class);

    private final String credentialName;

    public AptInstaller(Map<String, String> attributes)
    {
        this.credentialName = attributes.get("credentialName");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        SSH ssh = new SSH(host, d.getSpace(), credentialName);
        try {
            ssh.exec("sudo apt-get update");
            String out = ssh.exec("yes | sudo apt-get install " + uri.getFragment().replaceAll(",", " "));
            log.info(out);
            return out;
        }
        finally {
            ssh.close();
        }
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("will install the packages " + uri.getFragment());
    }
}

