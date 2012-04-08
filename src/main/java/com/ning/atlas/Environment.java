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
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.space.Space;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Environment
{
    private final List<Environment>   childEnvironments = new CopyOnWriteArrayList<Environment>();
    private final List<String>        listeners         = new CopyOnWriteArrayList<String>();
    private final Map<String, Base>   bases             = Maps.newConcurrentMap();
    private final Map<String, String> properties        = Maps.newConcurrentMap();

    private final ListMultimap<String, Uri<Installer>> virtualInstallers =
        Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, Uri<Installer>>create());

    private final PluginSystem         plugins;
    private final Collection<Template> environmentDefinedElements;
    private final String               name;
    private       Map<String, List<String>> cardinalityOverrides;


    public Environment()
    {
        this("lobby",
             new StaticPluginSystem(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, List<String>>emptyMap(),
             Collections.<String, Map<String, String>>emptyMap(),
             Collections.<String, Base>emptyMap(),
             Collections.<String, String>emptyMap(),
             Collections.<Template>emptyList(),
             Collections.<Environment>emptyList(),
             Collections.<String, List<String>>emptyMap());
    }

    public Environment(String name,
                       PluginSystem plugins,
                       Map<String, Map<String, String>> provisioners,
                       Map<String, Map<String, String>> installers,
                       Map<String, Map<String, String>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties)
    {
        this(name,
             plugins,
             provisioners,
             installers,
             Collections.<String, List<String>>emptyMap(),
             listeners,
             bases,
             properties,
             Collections.<Template>emptyList(),
             Collections.<Environment>emptyList(),
             Collections.<String, List<String>>emptyMap());
    }

    public Environment(String name,
                       PluginSystem plugins,
                       Map<String, Map<String, String>> provisioners,
                       Map<String, Map<String, String>> installers,
                       Map<String, List<String>> virtualInstallers,
                       Map<String, Map<String, String>> listeners,
                       Map<String, Base> bases,
                       Map<String, String> properties,
                       Collection<Template> environmentDefinedElements,
                       Collection<Environment> childEnvironments,
                       Map<String, List<String>> cardinalityOverrides)
    {
        this.name = name;
        this.plugins = plugins;
        this.environmentDefinedElements = ImmutableList.copyOf(environmentDefinedElements);
        this.childEnvironments.addAll(childEnvironments);
        this.cardinalityOverrides = ImmutableMap.copyOf(cardinalityOverrides);

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

        for (Map.Entry<String, List<String>> entry : virtualInstallers.entrySet()) {
            for (String uri : entry.getValue()) {
                this.virtualInstallers.put(entry.getKey(), Uri.<Installer>valueOf(uri));
            }
        }

        this.bases.putAll(bases);
        this.properties.putAll(properties);
    }

    public String getName()
    {
        return name;
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
            for (Environment childEnvironment : childEnvironments) {
                Maybe<Base> mb = childEnvironment.findBase(base);
                if (mb.isKnown()) {
                    return mb;
                }
            }
            return Maybe.unknown();
        }
    }

    public Map<String, String> getProperties()
    {
        Map<String, String> rs = Maps.newLinkedHashMap();
        for (Environment child : childEnvironments) {
            rs.putAll(child.getProperties());
        }
        rs.putAll(this.properties);
        return rs;
    }

    public ActualDeployment planDeploymentFor(SystemMap map, Space state)
    {
        return new ActualDeployment(map, this, state);
    }

    private Maybe<Provisioner> findProvisionerOnLocalEnv(String scheme)
    {
        return plugins.findProvisioner(scheme);
    }

    private Maybe<Installer> findInstallerOnLocalEnv(String scheme)
    {
        return plugins.findInstaller(scheme);
    }

    public Installer resolveInstaller(Uri<Installer> uri)
    {
        List<Environment> envs = Lists.newArrayList(this);
        envs.addAll(childEnvironments);
        for (Environment env : envs) {
            Maybe<Installer> mi = env.findInstallerOnLocalEnv(uri.getScheme());
            if (mi.isKnown()) {
                return mi.getValue();
            }
        }
        throw new IllegalStateException(String.format("No installer for %s available", uri.getScheme()));

    }

    public Provisioner resolveProvisioner(String scheme)
    {
        Maybe<Provisioner> mp = findProvisionerOnLocalEnv(scheme);
        if (mp.isKnown()) {
            return mp.getValue();
        }
        for (Environment childEnvironment : childEnvironments) {
            mp = childEnvironment.findProvisionerOnLocalEnv(scheme);
            if (mp.isKnown()) {
                return mp.getValue();
            }
        }
        throw new IllegalStateException("No provisioner matching " + scheme);
    }

    public List<LifecycleListener> getListeners()
    {
        List<LifecycleListener> rs = Lists.newArrayListWithExpectedSize(this.listeners.size());
        for (String prefix : listeners) {
            Maybe<LifecycleListener> ml = plugins.findListener(prefix);
            rs.add(ml.otherwise(new IllegalStateException("No listener available named " + prefix)));
        }
        for (Environment childEnvironment : childEnvironments) {
            rs.addAll(childEnvironment.getListeners());
        }
        return rs;
    }

    // exposed for testing
    PluginSystem getPluginSystem()
    {
        return this.plugins;
    }

    public Collection<Template> getEnvironmentDefinedElements()
    {
        List<Template> templates = Lists.newArrayList(environmentDefinedElements);
        for (Environment childEnvironment : childEnvironments) {
            templates.addAll(childEnvironment.getEnvironmentDefinedElements());
        }
        return templates;
    }

    public List<Uri<Installer>> expand(Uri<Installer> uri)
    {
        if (virtualInstallers.containsKey(uri.getScheme())) {
            return virtualInstallers.get(uri.getScheme());
        }
        else {

            for (Environment childEnvironment : childEnvironments) {
                List<Uri<Installer>> child_did_it = childEnvironment.expand(uri);
                if (!Collections.singletonList(child_did_it).equals(child_did_it)) {
                    return child_did_it;
                }
            }

            return Collections.singletonList(uri);
        }
    }

    public boolean isVirtual(Uri<Installer> uri)
    {
        if (virtualInstallers.containsKey(uri.getScheme())) {
            return true;
        }
        else {
            for (Environment childEnvironment : childEnvironments) {
                if (childEnvironment.isVirtual(uri)) {
                    return true;
                }
            }
            return false;
        }
    }

    List<String> overrideCardinality(Identity parent, String type, List<String> fromSystem)
    {
        String key;
        if (parent.isRoot()) {
            key = "/" + type;
        }
        else {
            key = parent.toExternalForm() + "/" + type;
        }

        if (this.cardinalityOverrides.containsKey(key)) {
            return this.cardinalityOverrides.get(key);
        }
        else {
            return fromSystem;
        }
    }
}
