package com.ning.atlas.plugin;

import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;

import java.util.List;
import java.util.Map;

public interface PluginSystem
{
    Maybe<Provisioner> findProvisioner(String provisioner);
    Maybe<LifecycleListener> findListener(String prefix);
    Maybe<Installer> findInstaller(String scheme);

    void registerProvisioner(String prefix, Class<? extends Provisioner> type, Map<String, String> args);
    void registerProvisionerConfig(String prefix, Map<String, String> args);

    void registerInstaller(String prefix, Class<? extends Installer> type, Map<String, String> args);
    void registerInstallerConfig(String prefix, Map<String, String> args);

    void registerListener(String prefix, Class<? extends LifecycleListener> type, Map<String, String> args);
    void registerListenerConfig(String prefix, Map<String, String> args);
}
