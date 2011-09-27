package com.ning.atlas.virtualbox;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.Base;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.Server;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.base.DoRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VBoxProvisioner implements Provisioner
{
	/*
	 * Stuff to take note of:
	 * - Time taken for VM to boot up cannot be > BOOTUP_TIME + BOOTUP_ADDITIONAL_TIMEOUT
	 * - VBoxManage is not multi-process safe, so know what you are doing
	 */

	private final static Logger logger = LoggerFactory.getLogger(VBoxProvisioner.class);

	private final String public_key;
	private final String intnet_name, bridgedif_name;

	private final static int BOOTUP_TIME = 20; // seconds
	private final static int BOOTUP_ADDITIONAL_TIMEOUT = 5 * 60; // seconds (5 minutes)

	// NIC number of the adapters in our image (NIC numbering starts from 1)
	private final static int NET_BRIDGED_ADAPTER = 1;
	private final static int NET_INTNET_ADAPTER = 2;


	public VBoxProvisioner(Map<String, String> attributes) throws UnableToProvisionServerException
	{
		this.intnet_name = attributes.get("intnet_name");
		this.bridgedif_name = attributes.get("bridgedif_name");

		String pub_key_file = attributes.get("pub_key_file");

		// read public key
		try {
			FileReader input = null;
			input = new FileReader(pub_key_file);
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
			throw new UnableToProvisionServerException("Unable to read public key from " + pub_key_file);
		}
	}


	@Override
	public Server provision(Base base, Node node) throws UnableToProvisionServerException
	{
		/*
		 * Plan of attack:
		 * - Import VM
		 * - Configure hardware
		 * - Get ip addresses
		 * - Start VM
		 * - Done
		 *
		 * Assumptions:
		 * - The 2 required virtual network interface cards are there in the required order
		 * - Connected to the WWW
		 * - VBox Guest Additions are installed
		 */

		final String username = base.getAttributes().get("username");
		final String password = base.getAttributes().get("password");
		final String vmname;

		logger.debug("provisioning server for base {}", base.getName());

		// import and find the name of the new vm

		String res;
		// Want to prevent name mangling with import
		synchronized (this) {
			res = DoRuntime.exec("VBoxManage", "import", base.getAttributes().get("image"));
			logger.trace("Dump from `VBoxManage import`:\n" + res);
		}

		// Output:
		// VM name specified with --vmname: "somename"	OR
		// Suggested VM name "somename"
		Pattern p = Pattern.compile("VM name.*?\"(.*?)\"");

		Matcher m = p.matcher(res);
		if (m.find())
			vmname = m.group(1);
		else
			throw new UnableToProvisionServerException("Unable to get virtual machine name");
		logger.info("VM name is {}", vmname);


		logger.trace(DoRuntime.exec("VBoxManage", "modifyvm", vmname,
				// Update bridgedif_name
				"--nic" + NET_BRIDGED_ADAPTER, "bridged", "--bridgeadapter" + NET_BRIDGED_ADAPTER, this.bridgedif_name, "--nicpromisc" + NET_BRIDGED_ADAPTER, "allow-all",
				// Update hostonlyif_name
				"--nic" + NET_INTNET_ADAPTER, "intnet", "--intnet" + NET_INTNET_ADAPTER, this.intnet_name, "--nicpromisc" + NET_INTNET_ADAPTER, "allow-vms"
		));


		// DoRuntime.exec("VBoxHeadless", "--vdre", "off", "--startvm", vmname));
		logger.trace(DoRuntime.exec("VBoxManage", "startvm", vmname));


		// Wait for virtual machine to boot up
		try {
			logger.info("Going to sleep for {} seconds while waiting for vm to boot", VBoxProvisioner.BOOTUP_TIME);
			Thread.sleep(VBoxProvisioner.BOOTUP_TIME * 1000);
		} catch (InterruptedException e) {
			throw new UnableToProvisionServerException("Spurious wakeup from sleep: " + e.toString());
		}
		logger.info("Done sleeping");


		// Retrieve ifconfig
		// Poll until we get what we want, else timeout
		boolean found_ifconfig = false;
		String output = null, ifconfig = null;
		long give_up_at = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(BOOTUP_ADDITIONAL_TIMEOUT);
		do {
			output = DoRuntime.exec("VBoxManage", "guestcontrol", vmname, "exec", "--image", "/usr/bin/sudo",
					"--username", username, "--password", password, "--wait-exit", "--wait-stdout", "--", "dhclient");
			if (!output.contains("VBoxManage: error")) {
				found_ifconfig = true;
				logger.trace(output);
				ifconfig = DoRuntime.exec("VBoxManage", "guestcontrol", vmname, "exec", "--image", "/sbin/ifconfig",
						"--username", username, "--password", password, "--wait-exit", "--wait-stdout");
				logger.trace(ifconfig);
			} else if (System.currentTimeMillis() > give_up_at) {
				throw new UnableToProvisionServerException("Timeout waiting for VBox guestcontrol");
			} else {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					throw new UnableToProvisionServerException("Spurious wakeup from sleep: " + e.toString());
				}
			}
		} while(!found_ifconfig);


		// Parse the ifconfig
		// IP addresses extraction
		String internalIp = null, externalIp = null;
		String[] sections = ifconfig.split("\n\n");
		Pattern pat_ip = Pattern.compile("(eth\\d+).*inet addr:(\\d+\\.\\d+\\.\\d+\\.\\d+)", Pattern.MULTILINE | Pattern.DOTALL);
		Pattern pat_ip6 = Pattern.compile("(eth\\d+).*inet6 addr: ([0-9a-f:]*)", Pattern.MULTILINE | Pattern.DOTALL);

		for (String section : sections) {
			// search for ipv4, else try ipv6
			boolean found_ip = true;
			Matcher m_ip = pat_ip.matcher(section);
			if (!m_ip.find()) {
				m_ip = pat_ip6.matcher(section);
				if (!m_ip.find()) found_ip = false;
			}

			if (found_ip) {
				String ethX = m_ip.group(1);
				// network interface numbering on *nix starts from 0 instead of 1
				if (ethX.equals("eth" + (NET_INTNET_ADAPTER - 1))) {
					internalIp = m_ip.group(2);
				} else if (ethX.equals("eth" + (NET_BRIDGED_ADAPTER - 1))) {
					externalIp = m_ip.group(2);
				}
			}
		}

		// IP check
		if (internalIp == null || externalIp == null) {
			throw new UnableToProvisionServerException("Cannot get ip addresses from ifconfig");
		} else {
			logger.info("Found: internalIp: {}, externalIp: {}", internalIp, externalIp);
		}

		SSH ssh;
		try {
			// Can use VBoxManage guestcontrol, but we want to check that SSH is working
			logger.info("Provisioning via SSH");
			ssh = new SSH(password, username, externalIp);

			// ssh into the server and append the public key
			ssh.exec("mkdir -p ~/.ssh; chmod 700 ~/.ssh; touch ~/.ssh/authorized_key;");
			ssh.exec("echo \"" + public_key + "\" >> ~/.ssh/authorized_keys");

			// check that networking is OK by doing a ping
			String hostcheck = "www.google.com";
			String ping = ssh.exec("ping -c 1 " + hostcheck);
			if (!ping.toLowerCase().contains("ttl")) {
				throw new UnableToProvisionServerException("Ping to " + hostcheck + " failed: " + ping);
			}

			ssh.close();
		} catch (IOException e) {
			throw new UnableToProvisionServerException("Provisioning via SSH failed: " + e.toString());
		}


		return vboxServer(base, vmname, internalIp, externalIp);
	}

	public void destroy(Server server) throws UnableToProvisionServerException {
		DoRuntime.exec("VBoxManage", "controlvm", server.getAttributes().get("vmname"), "poweroff");
		DoRuntime.exec("VBoxManage", "unregistervm", server.getAttributes().get("vmname"), "--delete");
	}

    private static Server vboxServer(Base base, String vmname, String internalIp, String externalIp) {
        return new Server(internalIp, externalIp, ImmutableMap.<String, String>of("vmname", vmname));
    }

//	public final class VBoxServer extends Server
//	{
//		private final String vmname;
//
//		public VBoxServer(Base base, String vmname, String internalIp, String externalIp) {
//			super(internalIp, externalIp, base);
//			this.vmname = vmname;
//		}
//
//		public String getVMName() {
//			return this.vmname;
//		}
//
//	}
}
