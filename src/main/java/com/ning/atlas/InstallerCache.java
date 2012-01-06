package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class InstallerCache
{
    private final Deployment d;
    private final Environment env;
    private final ConcurrentMap<String, Installer> concreteInstallers = Maps.newConcurrentMap();

    InstallerCache(Deployment d)
    {
        this.d = d;
        this.env = d.getEnvironment();
    }

    public List<Pair<Uri<Installer>, Installer>> lookup(Uri<Installer> uri)
    {
        if (concreteInstallers.containsKey(uri.getScheme())) {
            return Collections.singletonList(Pair.of(uri, concreteInstallers.get(uri.getScheme())));
        }
        if (!env.isVirtual(uri)) {
            Installer i = env.findInstaller(uri);
            i.start(d);
            concreteInstallers.put(uri.getScheme(), i);
            return Collections.singletonList(Pair.of(uri, i));
        }

        List<Uri<Installer>> v_uris = env.expand(uri);
        List<Pair<Uri<Installer>, Installer>> rs = Lists.newArrayList();
        for (Uri<Installer> v_uri : v_uris) {
            rs.addAll(lookup(v_uri));
        }
        return rs;
    }

    public void finished() {
        for (Installer installer : concreteInstallers.values()) {
            installer.finish(d);
        }
    }
}
