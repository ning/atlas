package com.ning.atlas.plugin;

import com.google.common.collect.Maps;
import com.ning.atlas.AtlasInstaller;
import com.ning.atlas.components.ExecInstaller;
import com.ning.atlas.Instantiator;
import com.ning.atlas.components.PrettyTerminalListener;
import com.ning.atlas.components.SSHKeyPairGenerator;
import com.ning.atlas.components.ScratchInstaller;
import com.ning.atlas.components.WaitForScratchValueInstaller;
import com.ning.atlas.components.aws.AWSConfigurator;
import com.ning.atlas.components.aws.EC2Provisioner;
import com.ning.atlas.components.aws.EC2SecurityGroupProvisioner;
import com.ning.atlas.components.aws.ELBAddInstaller;
import com.ning.atlas.components.aws.ELBProvisioner;
import com.ning.atlas.components.aws.RDSProvisioner;
import com.ning.atlas.components.aws.RDSSecurityGroupProvisioner;
import com.ning.atlas.components.chef.UbuntuChefSoloInstaller;
import com.ning.atlas.components.databases.OracleLoaderInstaller;
import com.ning.atlas.components.files.ERBFileInstaller;
import com.ning.atlas.components.files.FileInstaller;
import com.ning.atlas.components.files.ScriptInstaller;
import com.ning.atlas.components.galaxy.GalaxyInstaller;
import com.ning.atlas.components.galaxy.MicroGalaxyInstaller;
import com.ning.atlas.components.noop.NoOpInstaller;
import com.ning.atlas.components.noop.NoOpProvisioner;
import com.ning.atlas.components.packages.AptInstaller;
import com.ning.atlas.components.packages.GemInstaller;
import com.ning.atlas.components.packages.TarballInstaller;
import com.ning.atlas.components.packages.ZipInstaller;
import com.ning.atlas.components.vmware.VMRunLocalProvisioner;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.LifecycleListener;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;

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
        registerProvisioner("vmware", VMRunLocalProvisioner.class, EMPTY_MAP);
        registerProvisioner("rds", RDSProvisioner.class, EMPTY_MAP);
        registerProvisioner("noop", NoOpProvisioner.class, EMPTY_MAP);
        registerProvisioner("elb", ELBProvisioner.class, EMPTY_MAP);
        registerProvisioner("ec2-security-group", EC2SecurityGroupProvisioner.class, EMPTY_MAP);
        registerProvisioner("rds-security-group", RDSSecurityGroupProvisioner.class, EMPTY_MAP);

        registerInstaller("elb-add", ELBAddInstaller.class, EMPTY_MAP);
        registerInstaller("scratch", ScratchInstaller.class, EMPTY_MAP);
        registerInstaller("noop", NoOpInstaller.class, EMPTY_MAP);
        registerInstaller("atlas", AtlasInstaller.class, EMPTY_MAP);
        registerInstaller("galaxy", GalaxyInstaller.class, EMPTY_MAP);
        registerInstaller("ugx", MicroGalaxyInstaller.class, EMPTY_MAP);
        registerInstaller("elb", ELBProvisioner.class, EMPTY_MAP);
        registerInstaller("oracle", OracleLoaderInstaller.class, EMPTY_MAP);
        registerInstaller("apt", AptInstaller.class, EMPTY_MAP);
        registerInstaller("gem", GemInstaller.class, EMPTY_MAP);
        registerInstaller("file", FileInstaller.class, EMPTY_MAP);
        registerInstaller("erb", ERBFileInstaller.class, EMPTY_MAP);
        registerInstaller("script", ScriptInstaller.class, EMPTY_MAP);
        registerInstaller("exec", ExecInstaller.class, EMPTY_MAP);
        registerInstaller("ubuntu-chef-solo", UbuntuChefSoloInstaller.class, EMPTY_MAP);
        registerInstaller("tgz", TarballInstaller.class, EMPTY_MAP);
        registerInstaller("zip", ZipInstaller.class, EMPTY_MAP);
        registerInstaller("wait-for", WaitForScratchValueInstaller.class, EMPTY_MAP);

        registerListener("aws-config", AWSConfigurator.class, EMPTY_MAP);
        registerListener("ssh-keypairs", SSHKeyPairGenerator.class, EMPTY_MAP);
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
                return (Maybe<Provisioner>) Maybe.definitely(Instantiator.create(type, args));
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
                return (Maybe<LifecycleListener>) Maybe.definitely(Instantiator.create(type, args));
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
                return (Maybe<Installer>) Maybe.definitely(Instantiator.create(type, args));
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
