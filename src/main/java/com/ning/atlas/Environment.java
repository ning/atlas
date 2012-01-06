package com.ning.atlas;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.ning.atlas.plugin.PluginSystem;
import com.ning.atlas.plugin.StaticPluginSystem;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.space.Space;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Environment
{
    private final List<String>        listeners  = Lists.newArrayList();
    private final Map<String, Base>   bases      = Maps.newConcurrentMap();
    private final Map<String, String> properties = Maps.newConcurrentMap();

    private final ListMultimap<String, Uri<Installer>> virtualInstallers =
        Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, Uri<Installer>>create());

    private final PluginSystem         plugins;
    private final Collection<Template> environmentDefinedElements;

    public Environment()
    {
        this(new StaticPluginSystem(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Base>emptyMap(),
             Collections.<String, String>emptyMap(),
             Collections.<Template>emptyList());
    }

    public Environment(PluginSystem plugins,
                       Map<String, Map<String, String>> provisioners,
                       Map<String, Map<String, String>> installers,
                       Map<String, Map<String, String>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties)
    {
        this(plugins, provisioners, installers, listeners, bases, properties, Collections.<Template>emptyList());
    }

    public Environment(PluginSystem plugins,
                       Map<String, Map<String, String>> provisioners,
                       Map<String, Map<String, String>> installers,
                       Map<String, Map<String, String>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties,
                       Collection<Template> environmentDefinedElements)
    {
        this.plugins = plugins;
        this.environmentDefinedElements = ImmutableList.copyOf(environmentDefinedElements);

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

    public Installer findInstaller(Uri<Installer> uri)
    {
        return plugins.findInstaller(uri.getScheme())
                      .otherwise(new IllegalStateException("No installer matches " + uri));

    }

    public Provisioner resolveProvisioner(String scheme)
    {
        return plugins.findProvisioner(scheme)
                      .otherwise(new IllegalStateException("unable to locate provisioner for " + scheme));
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

    PluginSystem getPluginSystem()
    {
        return this.plugins;
    }

    public Collection<Template> getEnvironmentDefinedElements()
    {
        return environmentDefinedElements;
    }

    public List<Uri<Installer>> expand(Uri<Installer> uri)
    {
        if (virtualInstallers.containsKey(uri.getScheme())) {
            return virtualInstallers.get(uri.getScheme());
        }
        else {
            return Collections.singletonList(uri);
        }
    }

    public boolean isVirtual(Uri<Installer> uri)
    {
        return virtualInstallers.containsKey(uri.getScheme());
    }
}
