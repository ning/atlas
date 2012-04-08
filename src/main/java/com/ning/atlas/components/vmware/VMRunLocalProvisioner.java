package com.ning.atlas.components.vmware;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.PatternFilenameFilter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.skife.vmware.Guest;
import org.skife.vmware.VMRun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.CharStreams.readFirstLine;
import static java.util.Arrays.asList;

public class VMRunLocalProvisioner extends ConcurrentComponent
{

    private static final Set<String> CONFIG_KEYS = ImmutableSet.of("vmrun",
                                                                   "work_dir",
                                                                   "ssh_key_name",
                                                                   "tar",
                                                                   "creds");

    private final VMRun            vmrun;
    private final Map<String, URI> machineUrls;
    private final String           tar;
    private final String           credentialName;

    private final Map<String, String> config;

    public VMRunLocalProvisioner(Map<String, String> config)
    {
        this.config = ImmutableMap.copyOf(config);
        checkArgument(config.containsKey("vmrun"), "vmrun argument required");
        File vmrun_path = new File(config.get("vmrun"));
        checkArgument(vmrun_path.exists(), "Bad path for vmrun executable given: " + vmrun_path.getPath());

        if (config.containsKey("tar")) {
            this.tar = new File(config.get("tar")).getAbsolutePath();
        }
        else {
            tar = "tar";
        }
        this.credentialName = config.containsKey("creds") ? config.get("creds") : "default";

        vmrun = VMRun.withExecutableAt(vmrun_path);

        Set<String> vm_names = Sets.difference(config.keySet(), CONFIG_KEYS);
        ImmutableMap.Builder<String, URI> builder = ImmutableMap.builder();
        for (String vm_name : vm_names) {
            URI vm_uri = URI.create(config.get(vm_name));
            builder.put(vm_name, vm_uri);
            Map<String, String> query_params = Splitter.on('&').withKeyValueSeparator("=").split(vm_uri.getQuery());
            checkArgument(query_params.containsKey("user"), "VM URL must have 'user' query param");
            checkArgument(query_params.containsKey("pass"), "VM URL must have 'pass' query param");

        }
        machineUrls = builder.build();
    }

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        File workDir;
        if (config.containsKey("work_dir")) {
            workDir = new File(config.get("work_dir"));
        }
        else {
            workDir = new File(d.getScratch().get("atlas.environment-directory").getValue(), "vmware");
            if (!workDir.exists()) {
                if (!workDir.mkdirs()) {
                    // was it created by another thread or did we fail to make it?
                    if (!workDir.exists()) {
                        throw new IllegalStateException("Unable to create VM drectory" + workDir.getAbsolutePath());
                    }
                }
            }
        }

        Maybe<VMInfo> m_vmx = d.getSpace().get(host.getId(), VMInfo.class);
        final VMInfo vm_info;
        if (m_vmx.isKnown() && new File(m_vmx.getValue().getVmxPath()).exists()) {
            vm_info = m_vmx.getValue();
        }
        else {
            UUID id = UUID.randomUUID();
            String vm_name = uri.getFragment();
            final URI vm_uri = machineUrls.get(vm_name);
            Map<String, String> query_params = Splitter.on('&').withKeyValueSeparator("=").split(vm_uri.getQuery());
            String user = query_params.get("user");
            String pass = query_params.get("pass");

            File tmp_tarball = File.createTempFile("tmp", ".tgz");
            Files.copy(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return vm_uri.toURL().openStream();
                }
            }, tmp_tarball);

            File vmdir = new File(workDir, id.toString());
            if (!vmdir.exists()) {
                checkState(vmdir.mkdirs(), "unable to create virtual machine directory %s", vmdir.getAbsolutePath());
            }
            int exit = new ProcessBuilder().command(tar,
                                                    "-C", vmdir.getAbsolutePath(),
                                                    "-zxf", tmp_tarball.getAbsolutePath()).start().waitFor();

            checkState(exit == 0, "Unable to untar file from " + vm_uri.toString() + " with " +
                                  tar + ", need gnu tar. Pass 'tar' as argument " +
                                  " to provisioner config with correct path to gnu tar.");

            checkState((vmdir.listFiles()) != null && (vmdir.listFiles().length == 1),
                       "vm tarball must have exactly one directory in it, which " +
                       "should contain the vm (ie, the vmx file and its friends");

            File root = vmdir.listFiles()[0];
            List<File> chilluns = asList(root.listFiles(new PatternFilenameFilter(Pattern.compile(".+\\.vmx"))));
            File vmx = Iterables.getOnlyElement(chilluns);

            vm_info = new VMInfo();
            vm_info.setVmxPath(vmx.getAbsolutePath());
            vm_info.setPass(pass);
            vm_info.setUser(user);
            vm_info.setVmDir(vmdir.getAbsolutePath());

            d.getSpace().store(host.getId(), vm_info);

            // copy ssh credentials up
            SSHCredentials creds = SSHCredentials.lookup(d.getSpace(), credentialName)
                                                 .otherwise(new IllegalStateException("no credentials named '" +
                                                                                      credentialName + "' available"));

            Guest guest = vmrun.createGuest(vmx, user, pass);
            guest.startWithGUI();
            guest.sh("mkdir $HOME/.ssh");
            guest.copyFileToGuest(new File(creds.getKeyFilePath() + ".pub"), "/tmp/authorized_keys2");
            guest.sh("cat /tmp/authorized_keys2 >> $HOME/.ssh/authorized_keys2");
            guest.sh("chmod 0600 $HOME/.ssh/authorized_keys2");
        }

        // update space with correct IP address information
        Guest guest = vmrun.createGuest(vm_info.getVmxPath(), vm_info.getUser(), vm_info.getPass());
        guest.startWithGUI();

        String line = readFirstLine(guest.sh("ifconfig | grep 'inet addr' | egrep -v '127.0.0.1'").getStdoutSupplier());
        Matcher m = Pattern.compile("\\s+inet addr:([\\d\\.]+)\\s.+").matcher(line);
        m.matches();
        String ip = m.group(1);

        Server s = new Server();
        s.setExternalAddress(ip);
        s.setInternalAddress(ip);
        d.getSpace().store(host.getId(), s);

        return "running!";
    }


    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {

        Maybe<VMInfo> m_mvi = d.getSpace().get(hostId, VMInfo.class);
        if (m_mvi.isKnown()) {
            Guest g = vmrun.createGuest(m_mvi.getValue().getVmxPath(),
                                        m_mvi.getValue().getUser(),
                                        m_mvi.getValue().getPass());
            g.stop();
            new ProcessBuilder("rm", "-rf", m_mvi.getValue().getVmDir()).start().waitFor();
        }

        return null;
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("noop");
    }

    public static class VMInfo
    {
        private String vmxPath;
        private String vmDir;
        private String user;
        private String pass;

        public String getUser()
        {
            return user;
        }

        public void setUser(String user)
        {
            this.user = user;
        }

        public String getPass()
        {
            return pass;
        }

        public void setPass(String pass)
        {
            this.pass = pass;
        }

        public String getVmxPath()
        {
            return vmxPath;
        }

        public void setVmxPath(String vmxPath)
        {
            this.vmxPath = vmxPath;
        }

        public String getVmDir()
        {
            return vmDir;
        }

        public void setVmDir(String vmRoot)
        {
            this.vmDir = vmRoot;
        }
    }

}
