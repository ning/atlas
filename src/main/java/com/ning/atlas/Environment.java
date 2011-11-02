package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Environment
{
    private final Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners = Maps.newConcurrentMap();
    private final Map<String, Pair<Class<? extends Installer>, Map<String, String>>>   installers   = Maps.newConcurrentMap();
    private final List<Pair<Class<? extends LifecycleListener>, Map<String, String>>> listeners = new CopyOnWriteArrayList<Pair<Class<? extends LifecycleListener>, Map<String, String>>>();

    private final Map<String, Base>   bases      = Maps.newConcurrentMap();
    private final Map<String, String> properties = Maps.newConcurrentMap();

    public Environment()
    {
        this(Collections.<String, Pair<Class<? extends Provisioner>, Map<String, String>>>emptyMap(),
             Collections.<String, Pair<Class<? extends Installer>, Map<String, String>>>emptyMap(),
             Collections.<Pair<Class<? extends LifecycleListener>, Map<String, String>>>emptyList(),
             Collections.<String, Base>emptyMap(),
             Collections.<String, String>emptyMap());
    }

    public Environment(Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners,
                       Map<String, Pair<Class<? extends Installer>, Map<String, String>>> installers,
                       Collection<Pair<Class<? extends LifecycleListener>, Map<String, String>>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties)
    {
        this.provisioners.putAll(provisioners);
        this.installers.putAll(installers);
        this.bases.putAll(bases);
        this.properties.putAll(properties);
        this.listeners.addAll(listeners);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public Maybe<Base> findBase(final String base)
    {
        if (bases.containsKey(base)) {
            return Maybe.definitely(bases.get(base));
        }
        else {
            return Maybe.unknown();
        }
    }

    public Map<String, String> getProperties()
    {
        return ImmutableMap.copyOf(this.properties);
    }

    public ActualDeployment planDeploymentFor(SystemMap map, Space state)
    {
        return new ActualDeployment(map, this, state);
    }

    public Maybe<Provisioner> findProvisioner(String provisioner)
    {
        if (provisioners.containsKey(provisioner)) {
            Pair<Class<? extends Provisioner>, Map<String, String>> pair = provisioners.get(provisioner);
            try {
                return Maybe.definitely(Instantiator.create(pair.getLeft(), pair.getRight()));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate provisioner " + provisioner, e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    public Maybe<Installer> findInstaller(Uri<Installer> uri)
    {
        if (installers.containsKey(uri.getScheme())) {
            Pair<Class<? extends Installer>, Map<String, String>> pair = installers.get(uri.getScheme());
            try {
                return Maybe.definitely(Instantiator.create(pair.getLeft(), pair.getRight()));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate provisioner", e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    public Provisioner resolveProvisioner(String scheme)
    {
        return findProvisioner(scheme).otherwise(new ErrorProvisioner());
    }

    public Installer resolveInstaller(Uri<Installer> uri)
    {
        return findInstaller(uri).otherwise(new ErrorInstaller(Collections.<String, String>emptyMap()));
    }

    public List<Pair<Class<? extends LifecycleListener>, Map<String, String>>> getListeners()
    {
        return listeners;
    }
}
