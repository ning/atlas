package com.ning.atlas.files;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Uri;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

public class FileInstaller extends ConcurrentComponent
{

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
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
        String from = itty.next();
        String to = itty.next();

        return Futures.immediateFuture("upload local file " + from + " to " + to);
    }
}
