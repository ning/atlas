package com.ning.atlas.components.files;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

public class FileInstaller extends ConcurrentComponent
{
    private final Logger log = Logger.get(FileInstaller.class);

    private final String credentials;

    public FileInstaller(Map<String, String> attr)
    {
        this.credentials = attr.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        SSH ssh = new SSH(host, d.getSpace(), credentials);

        Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
        String from = itty.next();
        String to = itty.next();

        try {
            ssh.scpUpload(new File(from), "/tmp/hahaha");
            ssh.exec("sudo mv /tmp/hahaha %s", to);
        }
        finally {
            ssh.close();
        }
        return "copied " + from + " to " + to;
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        log.info("unwinding %s on %s", uri, hostId);
        SSH ssh = new SSH(hostId, credentials, d.getSpace());
        try {
            Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
            itty.next(); // from
            String to = itty.next();
            String out = ssh.exec("sudo rm " + to);
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
        Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
        String from = itty.next();
        String to = itty.next();

        return Futures.immediateFuture("upload local file " + from + " to " + to);
    }
}
