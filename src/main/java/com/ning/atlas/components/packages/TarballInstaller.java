package com.ning.atlas.components.packages;

import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;

import java.util.Map;
import java.util.concurrent.Future;

public class TarballInstaller extends ConcurrentComponent
{

    private final String credentialName;

    public TarballInstaller(Map<String, String> props)
    {
        this.credentialName = props.get("credentials");
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        String url = uri.getFragment();
        String to = uri.getParams().get("to");
        Maybe<String> skiproot = Maybe.elideNull(uri.getParams().get("skiproot"));

        final SSH ssh = new SSH(host.getId(), credentialName, d.getSpace());
        try {
            ssh.exec("mkdir /tmp/wafflehut");
            ssh.exec("curl %s > /tmp/wafflehut/foo.tar.gz", url);
            ssh.exec("mkdir /tmp/pancaketent");
            ssh.exec("tar -C /tmp/pancaketent -zxvf /tmp/wafflehut/foo.tar.gz");

            if (skiproot.isKnown()) {
                ssh.exec("sudo mv /tmp/pancaketent/%s/* %s/", skiproot.getValue(), to);
            }
            else {
                ssh.exec("sudo mv /tmp/pancaketent/* %s/", to);
            }
        }
        finally {
            ssh.close();
        }

        return "installed the tarball!";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
