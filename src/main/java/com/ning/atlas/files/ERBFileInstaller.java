package com.ning.atlas.files;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Uri;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

import static java.lang.String.format;

public class ERBFileInstaller extends ConcurrentComponent
{
    private final static Logger log = Logger.get(ERBFileInstaller.class);
    private final String creds;

    public ERBFileInstaller(Map<String, String> attrs)
    {
        this.creds = attrs.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
        String from = itty.next();
        String to = itty.next();

        ScriptingContainer container = new ScriptingContainer();
        container.put("$deployment", d);
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);

        String out = String.valueOf(container.runScriptlet(format("require 'erb'\n" +
                                                                  "require 'java'\n" +
                                                                  "ERB.new(File.read('%s')).result(binding)", from)));
        File tmp = File.createTempFile("atlas", ".tmp");
        Files.write(out.getBytes("UTF8"), tmp);

        SSH ssh = new SSH(host, d.getSpace(), creds);
        try {
            ssh.scpUpload(tmp, "/tmp/hahaha");
            ssh.exec("sudo mv /tmp/hahaha %s", to);
        }
        finally {
            ssh.close();
            tmp.delete();
        }

        log.info(out);
        return out;
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        Iterator<String> itty = Splitter.on('>').trimResults().split(uri.getFragment()).iterator();
        String from = itty.next();
        String to = itty.next();

        return Futures.immediateFuture(format("process %s and upload it to %s", from, to));
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        log.info("unwinding %s on %s", uri, hostId);
        SSH ssh = new SSH(hostId, creds, d.getSpace());
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
}
