package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.tree.Trees;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Host
{
    private final Identity             id;
    private final My                   my;
    private final Base                 base;
    private final List<Uri<Installer>> installs;
    private final Map<String, String>  environmentProperties;
    private AtomicReference<Server> server;

    public Host(Identity id,
                 My my,
                 Base base,
                 List<Uri<Installer>> installs,
                 Map<String, String> environmentProperties)
    {
        this.id = id;
        this.my = my;
        this.base = base;
        this.installs = installs;
        this.environmentProperties = environmentProperties;
    }

    public static List<Host> findHostsIn(NormalizedTemplate root, Environment env)
    {
        List<NormalizedServerTemplate> xs = Trees.findInstancesOf(root, NormalizedServerTemplate.class);
        for (NormalizedServerTemplate template : xs) {
            Base base = env.findBase(template.getBase()).otherwise(Base.errorBase(template.getBase(), env));

            Uri p = base.getProvisioner();
            List<Uri<Installer>> installs = template.getInstallations();

            Host h = new Host(template.getId(),
                              template.getMy(),
                              base,
                              installs,
                              env.getProperties());
        }
        return null;
    }

    public Uri<Provisioner> getProvisioner()
    {
        return base.getProvisioner();
    }

    public Base getBase()
    {
        return base;
    }

    public void addError(Throwable e)
    {

    }

    public void setServer(Server s)
    {
        this.server.set(s);
    }
}
