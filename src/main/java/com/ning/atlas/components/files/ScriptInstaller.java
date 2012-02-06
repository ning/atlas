package com.ning.atlas.components.files;

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
import java.util.Map;
import java.util.concurrent.Future;

public class ScriptInstaller extends ConcurrentComponent
{

    private static final Logger log = Logger.get(ScriptInstaller.class);

    private final String creds;

    public ScriptInstaller(Map<String, String> attrs)
    {
        this.creds = attrs.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        File script = new File(uri.getFragment());

        SSH ssh = new SSH(host, d.getSpace(), creds);
        try {
            ssh.scpUpload(script, "/tmp/script_installer_script");
            ssh.exec("chmod +x /tmp/script_installer_script");
            String out = ssh.exec("/tmp/script_installer_script");
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
        return Futures.immediateFuture(String.format("will execute %s remotely", uri.getFragment()));
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new IllegalStateException("no unwinding of a script, sorry");
    }
}
