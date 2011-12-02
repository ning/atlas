package com.ning.atlas.plugin;

import com.google.common.collect.Maps;
import com.ning.atlas.AtlasInstaller;
import com.ning.atlas.ErrorInstaller;
import com.ning.atlas.ErrorProvisioner;
import com.ning.atlas.ExecInstaller;
import com.ning.atlas.Instantiator;
import com.ning.atlas.PrettyTerminalListener;
import com.ning.atlas.ScratchInstaller;
import com.ning.atlas.aws.AWSConfigurator;
import com.ning.atlas.aws.EC2Provisioner;
import com.ning.atlas.aws.ELBInstaller;
import com.ning.atlas.aws.RDSProvisioner;
import com.ning.atlas.chef.UbuntuChefSoloInstaller;
import com.ning.atlas.databases.OracleLoaderInstaller;
import com.ning.atlas.files.ERBFileInstaller;
import com.ning.atlas.files.FileInstaller;
import com.ning.atlas.files.ScriptInstaller;
import com.ning.atlas.galaxy.GalaxyInstaller;
import com.ning.atlas.galaxy.MicroGalaxyInstaller;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.noop.NoOpInstaller;
import com.ning.atlas.noop.NoOpProvisioner;
import com.ning.atlas.packages.AptInstaller;
import com.ning.atlas.packages.GemInstaller;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import org.apache.commons.lang3.tuple.Pair;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

public class StaticPluginSystem implements PluginSystem
{
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    private final Map<String, Class<? extends Provisioner>>       provisioners = Maps.newConcurrentMap();
    private final Map<String, Class<? extends Installer>>         installers   = Maps.newConcurrentMap();
    private final Map<String, Class<? extends LifecycleListener>> listeners    = Maps.newConcurrentMap();

    private final Map<String, Map<String, String>> provisionerConfigs = Maps.newConcurrentMap();
    private final Map<String, Map<String, String>> installerConfigs   = Maps.newConcurrentMap();
    private final Map<String, Map<String, String>> listenerConfigs    = Maps.newConcurrentMap();

    public StaticPluginSystem()
    {
        registerProvisioner("ec2", EC2Provisioner.class, EMPTY_MAP);
        registerProvisioner("rds", RDSProvisioner.class, EMPTY_MAP);
        registerProvisioner("noop", NoOpProvisioner.class, EMPTY_MAP);

        registerInstaller("scratch", ScratchInstaller.class, EMPTY_MAP);
        registerInstaller("noop", NoOpInstaller.class, EMPTY_MAP);
        registerInstaller("atlas", AtlasInstaller.class, EMPTY_MAP);
        registerInstaller("galaxy", GalaxyInstaller.class, EMPTY_MAP);
        registerInstaller("ugx", MicroGalaxyInstaller.class, EMPTY_MAP);
        registerInstaller("elb", ELBInstaller.class, EMPTY_MAP);
        registerInstaller("oracle", OracleLoaderInstaller.class, EMPTY_MAP);
        registerInstaller("apt", AptInstaller.class, EMPTY_MAP);
        registerInstaller("gem", GemInstaller.class, EMPTY_MAP);
        registerInstaller("file", FileInstaller.class, EMPTY_MAP);
        registerInstaller("erb", ERBFileInstaller.class, EMPTY_MAP);
        registerInstaller("script", ScriptInstaller.class, EMPTY_MAP);
        registerInstaller("exec", ExecInstaller.class, EMPTY_MAP);
        registerInstaller("ubuntu-chef-solo", UbuntuChefSoloInstaller.class, EMPTY_MAP);

        registerListener("aws-config", AWSConfigurator.class, EMPTY_MAP);
        registerListener("progress-bars", PrettyTerminalListener.class, EMPTY_MAP);
    }

    @Override
    public void registerProvisioner(String prefix, Class<? extends Provisioner> type, Map<String, String> args)
    {
        provisioners.put(prefix, type);
        provisionerConfigs.put(prefix, args);
    }

    @Override
    public void registerProvisionerConfig(String prefix, Map<String, String> args)
    {
        provisionerConfigs.put(prefix, args);
    }

    @Override
    public void registerListener(String prefix, Class<? extends LifecycleListener> type, Map<String, String> args)
    {
        listeners.put(prefix, type);
        listenerConfigs.put(prefix, args);
    }

    @Override
    public void registerListenerConfig(String prefix, Map<String, String> args)
    {
        listenerConfigs.put(prefix, args);
    }

    @Override
    public void registerInstaller(String prefix, Class<? extends Installer> type, Map<String, String> args)
    {
        installers.put(prefix, type);
        installerConfigs.put(prefix, args);
    }

    @Override
    public void registerInstallerConfig(String prefix, Map<String, String> args)
    {
        installerConfigs.put(prefix, args);
    }

    @Override
    public Maybe<Provisioner> findProvisioner(String provisioner)
    {
        if (provisioners.containsKey(provisioner)) {
            Class<? extends Provisioner> type = provisioners.get(provisioner);
            Map<String, String> args = provisionerConfigs.get(provisioner);
            try {
                return Maybe.definitely(Instantiator.create(type, args));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate provisioner " + provisioner, e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    @Override
    public Maybe<LifecycleListener> findListener(String prefix)
    {
        if (listeners.containsKey(prefix)) {
            Class<? extends LifecycleListener> type = listeners.get(prefix);
            Map<String, String> args = listenerConfigs.get(prefix);
            try {
                return Maybe.definitely(Instantiator.create(type, args));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate listener " + prefix, e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }

    @Override
    public Maybe<Installer> findInstaller(String scheme)
    {
        if (installers.containsKey(scheme)) {
            Class<? extends Installer> type = installers.get(scheme);
            Map<String, String> args = installerConfigs.get(scheme);
            try {
                return Maybe.definitely(Instantiator.create(type, args));
            }
            catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate installer " + scheme, e);
            }
        }
        else {
            return Maybe.unknown();
        }
    }
}
