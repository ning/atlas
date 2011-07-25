package com.ning.atlas.virtualbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ning.atlas.SSH;

import com.ning.atlas.Base;
import com.ning.atlas.DoRuntime;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import com.ning.atlas.UnableToProvisionServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VBoxProvisioner implements Provisioner
{
	/*
	 * Stuff to take note of:
	 * - Time taken for VM to boot up cannot be > sleep time + SSH timeout
	 * - Being able to SSH in does not mean that all the NICs are up
	 * - VBoxManage is not multi-process safe, so know what you are doing
	 */
	
    private static boolean dhcp_done = false;
	
    private final static Logger logger = LoggerFactory.getLogger(VBoxProvisioner.class);
    private final static AtomicInteger portCounter = new AtomicInteger(0);
    
    private String vmname = null;
    private String username;
    private String password;
    private String pub_key_file; // path to file containing public key
    private int port_start;
    private String hostonlyif_name, bridgedif_name;
    private int sshport;
    
    private final static int BOOTUP_TIME = 20; // seconds
    private final static int DEFAULT_PORT = 2222;
    
    // NIC number of the adapters in our image (NIC numbering starts from 1)
    private final static int NET_HOSTONLY_ADAPTER = 1;
    private final static int NET_BRIDGED_ADAPTER = 2;
    private final static int NET_NAT = 3;

    public VBoxProvisioner(Map<String, String> attributes)
    {
        this.username = attributes.get("username");
        this.password = attributes.get("password");
        this.pub_key_file = attributes.get("pub_key_file");
        this.hostonlyif_name = attributes.get("hostonlyif_name");
        this.bridgedif_name = attributes.get("bridgedif_name");
        
        if (!attributes.containsKey("port")) {
        	this.port_start = VBoxProvisioner.DEFAULT_PORT;
        } else {
        	this.port_start = Integer.parseInt(attributes.get("port_start"));
        }
    }
    
    
	@Override
	public Server provision(Base base) throws UnableToProvisionServerException
	{
		/*
		 * Plan of attack:
		 * - Setup DHCP
		 * - Import vm
		 * - Configure hardware
		 * - SSH in to get ip addresses
		 * - Start VM
		 * - Done
		 * 
		 * Assumptions:
		 * - The 3 required virtual networks cards are there in the required order
		 * - Connected to the WWW
		 */
		
        logger.debug("provisioning server for base {}", base.getName());
        
        synchronized (this) {
	        // Configure the DHCP (Only need to do this once)
        	if (!dhcp_done) {
		        DoRuntime.exec("VBoxManage", "hostonlyif", "ipconfig", this.hostonlyif_name, "--ip", "192.168.133.0"); // adapter
		        DoRuntime.exec("VBoxManage", "dhcpserver", "modify", "--ifname", this.hostonlyif_name, 
		        	"--ip", "192.168.133.7", "--netmask", "255.255.255.0",
		        	"--lowerip", "192.168.133.8", "--upperip", "192.168.133.255", "--enable"); // dhcp
		        dhcp_done = true;
        	}
        }
        
        // import and find the name of the new vm
        
        String res;
        // Want to prevent name mangling with import
        synchronized (this) {
	        res = DoRuntime.exec("VBoxManage", "import", base.getAttributes().get("ovf"));
	        logger.trace("Dump from `VBoxManage import`:\n" + res);
        }
        
        // Output:
        // VM name specified with --vmname: "somename"	OR
        // Suggested VM name "somename"
		Pattern p = Pattern.compile("VM name.*?\"(.*?)\"");
		
		Matcher m = p.matcher(res);
		if (m.find()) this.vmname = m.group(1);
		logger.info("VM name is {}", this.vmname);
		
		this.sshport = this.port_start + portCounter.getAndIncrement();
        logger.info("Using port {} for ssh", this.sshport);
        
        
		logger.trace(DoRuntime.exec("VBoxManage", "modifyvm", this.vmname,
			// Update hostonlyif_name
			"--hostonlyadapter" + VBoxProvisioner.NET_HOSTONLY_ADAPTER, this.hostonlyif_name,
			// Update bridgedif_name
			"--bridgeadapter" + VBoxProvisioner.NET_BRIDGED_ADAPTER, this.bridgedif_name)
		);
		
		// Change port forwarding (handled separately since this might fail)
		// If port forwarding mapping already exist, output is:
		// VBoxManage: error: A NAT rule for this host port and this host IP already exists
		logger.trace(DoRuntime.exec("VBoxManage", "modifyvm", this.vmname,
			"--natpf" + VBoxProvisioner.NET_NAT, "ssh,tcp,," + sshport + ",,22"));
        
		
		// DoRuntime.exec("VBoxHeadless", "--vdre", "off", "--startvm", this.vmname));
		logger.trace(DoRuntime.exec("VBoxManage", "startvm", this.vmname));
		
		// read public key
		String public_key;
		try {
	        FileReader input = null;
			input = new FileReader(this.pub_key_file);
			BufferedReader bufRead = new BufferedReader(input);
			String line;
			StringBuilder sb = new StringBuilder();
			String newLine = "\n";
			while ((line = bufRead.readLine()) != null) {
				sb.append(line).append(newLine);
			}
	        public_key = sb.toString();
	        logger.info("Public key obtained");
	        logger.trace("Public key is {}", public_key);
		} catch (Exception e) {
			throw new UnableToProvisionServerException("Unable to read public key: " + e.toString());
		}
		
		// wait for virtual machine to boot up
		try {
			logger.info("Going to sleep for {} seconds while waiting for vm to boot", VBoxProvisioner.BOOTUP_TIME);
			Thread.sleep(VBoxProvisioner.BOOTUP_TIME * 1000);
		} catch (InterruptedException e) {
			throw new UnableToProvisionServerException("Spurious wakeup from sleep: " + e.toString());
		}
		logger.info("Done sleeping");
		
		
		SSH ssh = null;
		String ifconfig = "";
		try {
			logger.info("SSH login attempt...");
			ssh = new SSH(this.password, this.username, "localhost", sshport);
			logger.info("SSH to localhost on port {} successful", sshport);
			
			// ssh into the server and append the public key
			ssh.exec("echo \"" + public_key + "\" >> ~/.ssh/authorized_keys");
			
			// get the ip addresses
			// time on vm != time on host, so sleep on the vm side
			ifconfig = ssh.exec("sleep 10; ifconfig");
			logger.trace("ifconfig output:\n" + ifconfig);
			
			ssh.close();
		} catch (IOException e) {
			throw new UnableToProvisionServerException("Provisioning via SSH failed: " + e.toString());
		}
		
		
		// IP addresses extraction
		String internalIp = null, externalIp = null;
		String[] sections = ifconfig.split("\n\n");
		Pattern pat_ip = Pattern.compile("(eth\\d+).*inet addr:(\\d+\\.\\d+\\.\\d+\\.\\d+)", Pattern.MULTILINE | Pattern.DOTALL);
		
		for (String section : sections) {
			Matcher m_ip = pat_ip.matcher(section);
			if (m_ip.find()) {
				String ethX = m_ip.group(1);
				// network interface numbering on *nix starts from 0 instead of 1
				if (ethX.equals("eth" + (VBoxProvisioner.NET_HOSTONLY_ADAPTER - 1))) {
					internalIp = m_ip.group(2);
				} else if (ethX.equals("eth" + (VBoxProvisioner.NET_BRIDGED_ADAPTER - 1))) {
					externalIp = m_ip.group(2);
				}
			}
		}
		
		if (internalIp == null || externalIp == null) {
			throw new UnableToProvisionServerException("Cannot get ip addresses from ifconfig");
		} else {
			logger.info("Found internalIp: {}", internalIp);
			logger.info("Found externalIp: {}", externalIp);
		}
		
        return new VBoxServer(base, this.vmname, internalIp, externalIp, sshport);
	}
	
	public void destroy(Server server) throws UnableToProvisionServerException {
		VBoxServer vb = VBoxServer.class.cast(server);
		DoRuntime.exec("VBoxManage", "controlvm", vb.vmname, "poweroff");
		DoRuntime.exec("VBoxManage", "unregistervm", vb.vmname, "--delete");
	}

	public final class VBoxServer extends Server
	{
		private final String vmname;
		private final int sshPort; // port on host used for ssh port forwarding

		public VBoxServer(Base base, String vmname, String internalIp, String externalIp, int sshPort) {
			super(internalIp, externalIp, base);
			this.vmname = vmname;
			this.sshPort = sshPort;
		}

		public String getVMName() {
			return this.vmname;
		}
		
		public int getSSHPort() {
			return this.sshPort;
		}
	}		
}
