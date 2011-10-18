package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.tree.Trees;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Environment
{
    private final List<Base>               bases        = new CopyOnWriteArrayList<Base>();
    private final List<Environment>        children     = new CopyOnWriteArrayList<Environment>();
    private final Map<String, Provisioner> provisioners = Maps.newConcurrentMap();
    private final Map<String, Installer>   installers   = Maps.newConcurrentMap();
    private final Map<String, String>      properties   = Maps.newConcurrentMap();

    private final String             name;
    private final Maybe<Environment> parent;

    public Environment(String name)
    {
        this(name,
             Collections.<String, Provisioner>emptyMap(),
             Collections.<String, Installer>emptyMap(), null);
    }

    public Environment(String name,
                       Map<String, Provisioner> provisioners,
                       Map<String, Installer> installers)
    {
        this(name, provisioners, installers, null);
    }

    public Environment(String name,
                       Map<String, Provisioner> provisioners,
                       Map<String, Installer> installers,
                       @Nullable Environment parent)
    {
        this.name = name;
        this.parent = Maybe.elideNull(parent);
        this.provisioners.putAll(provisioners);
        this.installers.putAll(installers);
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
                      .add("children", children)
                      .add("bases", bases)
                      .toString();
    }

    public void addChild(Environment e)
    {
        this.children.add(e);
    }

    public Provisioner getProvisioner(Uri<Provisioner> name)
    {
        return provisioners.get(name.getScheme());
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

    public Map<String, Installer> getInstallers()
    {
        return Maps.newHashMap(installers);
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

        if (parent.isKnown()) {
            rs.putAll(parent.getValue().getProperties());
        }

        // override parent props with ours
        rs.putAll(this.properties);
        return rs;
    }

    public Map<String, Provisioner> getProvisioners()
    {
        return provisioners;
    }

    public Deployment planDeploymentFor(SystemMap map, Space state)
    {
        return new Deployment(map, this, state);
    }

    public Maybe<Provisioner> findProvisioner(Uri<Provisioner> provisioner)
    {
        if (provisioners.containsKey(provisioner.getScheme())) {
            return Maybe.definitely(provisioners.get(provisioner.getScheme()));
        }
        else {
            return Maybe.unknown();
        }
    }

    public Maybe<Installer> findInstaller(Uri<Installer> uri)
    {
        if (installers.containsKey(uri.getScheme())) {
            return Maybe.definitely(installers.get(uri.getScheme()));
        }
        else {
            return Maybe.unknown();
        }
    }

    public Provisioner resolveProvisioner(Uri<Provisioner> uri)
    {
        return findProvisioner(uri).otherwise(new ErrorProvisioner());
    }

    public Installer resolveInstaller(Uri<Installer> uri)
    {
        return findInstaller(uri).otherwise(new ErrorInstaller(Collections.<String, String>emptyMap()));
    }
}
