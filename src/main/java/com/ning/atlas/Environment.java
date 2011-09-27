package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.ning.atlas.badger.DeploymentPlan;
import com.ning.atlas.badger.Host;
import com.ning.atlas.badger.NormalizedTemplate;
import com.ning.atlas.badger.SystemMap;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.tree.MagicVisitor;
import com.ning.atlas.tree.Trees;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Environment
{
    private final List<Base>               bases        = new CopyOnWriteArrayList<Base>();
    private final List<Environment>        children     = new CopyOnWriteArrayList<Environment>();
    private final Map<String, Provisioner> provisioners = Maps.newConcurrentMap();
    private final Map<String, Installer>   initializers = Maps.newConcurrentMap();
    private final Map<String, Installer>   installers   = Maps.newConcurrentMap();
    private final Map<String, String>      properties   = Maps.newConcurrentMap();

    private final String             name;
    private final Maybe<Environment> parent;

    public Environment(String name)
    {
        this(name, Collections.<String, Provisioner>emptyMap(), Collections.<String, Installer>emptyMap(), null);
    }

    public Environment(String name,
                       Map<String, Provisioner> provisioners,
                       Map<String, Installer> initializers)
    {
        this(name, provisioners, initializers, null);
    }

    public Environment(String name,
                       Map<String, Provisioner> provisioners,
                       Map<String, Installer> initializers,
                       @Nullable Environment parent)
    {
        this.name = name;
        this.parent = Maybe.elideNull(parent);
        this.provisioners.putAll(provisioners);
        this.initializers.putAll(initializers);
    }

    public void addProvisioner(String name, Provisioner p)
    {
        this.provisioners.put(name, p);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", name)
                      .add("provisioners", provisioners)
                      .add("initializers", initializers)
                      .add("children", children)
                      .add("bases", bases)
                      .toString();
    }

    public void addChild(Environment e)
    {
        this.children.add(e);
    }

    public Provisioner getProvisioner(String name)
    {
        return provisioners.get(name);
    }

    public void addInitializer(String name, Installer init)
    {
        initializers.put(name, init);
    }

    public void addInstaller(String name, Installer installer)
    {
        installers.put(name, installer);
    }

    public Maybe<Base> findBase(final String base)
    {
        for (Base candidate : bases) {
            if (candidate.getName().equals(base)) {
                return Maybe.definitely(candidate);
            }
        }

        for (Environment child : children) {
            Maybe<Base> rs = child.findBase(base);
            if (rs.isKnown()) {
                return rs;
            }
        }

        return Maybe.unknown();
    }

    public void addBase(Base base)
    {
        bases.add(base);
    }

    public Map<String, Installer> getInitializers()
    {
        return initializers;
    }

    public Map<String, Installer> getInstallers()
    {
        return installers;
    }

    public List<Environment> getChildren()
    {
        return children;
    }

    public void addProperties(Map<String, String> props)
    {
        this.properties.putAll(props);
    }

    public Map<String, String> getProperties()
    {
        Map<String, String> rs = Maps.newHashMap();
        rs.putAll(parent.to(new Function<Environment, Map<String, String>>()
        {
            @Override
            public Map<String, String> apply(@Nullable Environment input)
            {
                return input.getProperties();
            }
        }).otherwise(Collections.<String, String>emptyMap()));

        // override parent props with ours
        rs.putAll(this.properties);
        return rs;
    }

    public Map<String, Provisioner> getProvisioners()
    {
        return provisioners;
    }

    public DeploymentPlan planDeploymentFor(NormalizedTemplate root, SystemMap from)
    {
        // find all hosts
        List<Host> hosts = Host.findHostsIn(root, this);

        // determine provision steps

        // determine init steps

        // determine install steps

        return null;
    }
}
