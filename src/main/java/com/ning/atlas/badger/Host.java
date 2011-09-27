package com.ning.atlas.badger;

import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.tree.Trees;

import java.util.List;
import java.util.Map;

public class Host
{
    private final Identity             id;
    private final My                   my;
    private final Base                 base;
    private final List<Uri<Installer>> installs;
    private final Map<String, String>  environmentProperties;

    private Host(Identity id,
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
}
