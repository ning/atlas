package com.ning.atlas.components.files;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import java.util.regex.Pattern;

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
        Iterator<String> argv = Splitter.on(Pattern.compile("\\s+")).split(uri.getFragment()).iterator();
        String script_path = argv.next();

        File script = new File(script_path);

        SSH ssh = new SSH(host, d.getSpace(), creds);
        try {
            ssh.scpUpload(script, "/tmp/script_installer_script");
            ssh.exec("chmod +x /tmp/script_installer_script");
            String out = ssh.exec("/tmp/script_installer_script " + Joiner.on(" ").join(Lists.newArrayList(argv)));
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
        if (uri.getParams().containsKey("unwind")) {
            log.info("unwinding " + uri + " with " + uri.getParams().get("unwind"));
            Iterator<String> argv = Splitter.on(Pattern.compile("\\s+")).split(uri.getParams().get("unwind")).iterator();
            String script_path = argv.next();

            File script = new File(script_path);

            SSH ssh = new SSH(hostId, creds, d.getSpace());
            try {
                ssh.scpUpload(script, "/tmp/script_installer_script");
                ssh.exec("chmod +x /tmp/script_installer_script");
                String out = ssh.exec("/tmp/script_installer_script " + Joiner.on(" ").join(Lists.newArrayList(argv)));
                log.info(out);
                return out;
            }
            finally {
                ssh.close();
            }

        } else {
            throw new IllegalStateException("no uwind parameter so cannot unwind");
        }
    }
}
