package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.plugin.PluginSystem;
import com.ning.atlas.plugin.StaticPluginSystem;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.space.Space;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Environment
{
    private final List<String> listeners = Lists.newArrayList();
    private final Map<String, Base>   bases      = Maps.newConcurrentMap();
    private final Map<String, String> properties = Maps.newConcurrentMap();

    private final PluginSystem plugins;

    public Environment()
    {
        this(new StaticPluginSystem(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Base>emptyMap(),
             Collections.<String, String>emptyMap());
    }

    public Environment(PluginSystem plugins,
                       Map<String, Map<String, String>> provisioners,
                       Map<String, Map<String, String>> installers,
                       Map<String, Map<String, String>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties)
    {
        this.plugins = plugins;
        for (Map.Entry<String, Map<String, String>> entry : provisioners.entrySet()) {
            plugins.registerProvisionerConfig(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Map<String, String>> entry : installers.entrySet()) {
            plugins.registerInstallerConfig(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Map<String, String>> entry : listeners.entrySet()) {
            plugins.registerListenerConfig(entry.getKey(), entry.getValue());
            this.listeners.add(entry.getKey());
        }

        this.bases.putAll(bases);
        this.properties.putAll(properties);
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
        return plugins.findProvisioner(provisioner);
    }

    public Maybe<Installer> findInstaller(String scheme)
    {
        return plugins.findInstaller(scheme);
    }

    public Provisioner resolveProvisioner(String scheme)
    {
        return plugins.findProvisioner(scheme).otherwise(new IllegalStateException("unable to locate provisioner for " +
                                                                                   scheme));
    }

    public Installer resolveInstaller(String scheme)
    {
        return plugins.findInstaller(scheme).otherwise(new IllegalStateException("unable to locate installer for " +
                                                                                 scheme));
    }

    public List<LifecycleListener> getListeners()
    {
        List<LifecycleListener> rs = Lists.newArrayListWithExpectedSize(this.listeners.size());
        for (String prefix : listeners) {
            Maybe<LifecycleListener> ml = plugins.findListener(prefix);
            rs.add(ml.otherwise(new IllegalStateException("No listener available named " + prefix)));
        }
        return rs;
    }

    public PluginSystem getPluginSystem()
    {
        return this.plugins;
    }
}
