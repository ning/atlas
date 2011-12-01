package com.ning.atlas.plugin;

import com.google.common.collect.Maps;
import com.ning.atlas.AtlasInstaller;
import com.ning.atlas.ErrorInstaller;
import com.ning.atlas.ErrorProvisioner;
import com.ning.atlas.ExecInstaller;
import com.ning.atlas.Instantiator;
import com.ning.atlas.ScratchInstaller;
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
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Provisioner;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Map;

public class StaticPluginSystem implements PluginSystem
{
    private static final Logger log = Logger.get(StaticPluginSystem.class);

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    private final Map<String, Pair<Class<? extends Provisioner>, Map<String, String>>> provisioners = Maps.newConcurrentMap();
    private final Map<String, Pair<Class<? extends Installer>, Map<String, String>>>   installers   = Maps.newConcurrentMap();


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
    }

    @Override
    public void registerProvisioner(String prefix, Class<? extends Provisioner> type, Map<String, String> args)
    {
        provisioners.put(prefix, Pair.<Class<? extends Provisioner>, Map<String, String>>of(type, args));
    }

    @Override
    public void registerProvisionerConfig(String prefix, Map<String, String> args)
    {

        if (provisioners.containsKey(prefix)) {
            Pair<Class<? extends Provisioner>, Map<String, String>> old = provisioners.get(prefix);
            Pair<Class<? extends Provisioner>, Map<String, String>> noo =
                Pair.<Class<? extends Provisioner>, Map<String, String>>of(old.getKey(), args);
            provisioners.put(prefix, noo);
        }
        else {
            throw new IllegalStateException("asked to configure a non existent provisioner: " + prefix);
        }


    }

    @Override
    public void registerInstaller(String prefix, Class<? extends Installer> type, Map<String, String> args)
    {
        installers.put(prefix, Pair.<Class<? extends Installer>, Map<String, String>>of(type, args));
    }

    @Override
    public void registerInstallerConfig(String prefix, Map<String, String> args)
    {
        if (installers.containsKey(prefix)) {
            Pair<Class<? extends Installer>, Map<String, String>> old = installers.get(prefix);

            Pair<Class<? extends Installer>, Map<String, String>> noo =
                Pair.<Class<? extends Installer>, Map<String, String>>of(old.getKey(), args);
            installers.put(prefix, noo);
        }
        else {
            throw new IllegalStateException("asked to configure a non existent installer: " + prefix);
        }
    }

    @Override
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

    @Override
    public Maybe<Installer> findInstaller(String scheme)
    {
        if (installers.containsKey(scheme)) {
            Pair<Class<? extends Installer>, Map<String, String>> pair = installers.get(scheme);
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
}
