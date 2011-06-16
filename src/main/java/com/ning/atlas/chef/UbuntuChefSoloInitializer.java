package com.ning.atlas.chef;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import org.antlr.stringtemplate.StringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class UbuntuChefSoloInitializer implements Initializer
{
    private final static Logger logger = LoggerFactory.getLogger(UbuntuChefSoloInitializer.class);

    private final String          sshUser;
    private final String          sshKeyFile;
    private final File            chefSoloInitFile;
    private final File            soloRbFile;

    public UbuntuChefSoloInitializer(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");

        final String recipeUrl = attributes.get("recipe_url");
        checkNotNull(recipeUrl, "recipe_url attribute required");
        try {
            this.chefSoloInitFile = File.createTempFile("chef-solo-init", "sh");
            InputStream in = UbuntuChefSoloInitializer.class.getResourceAsStream("/ubuntu-chef-solo-setup.sh");
            Files.write(ByteStreams.toByteArray(in), this.chefSoloInitFile);
            in.close();

            this.soloRbFile = File.createTempFile("solo", "rb");
            InputStream in2 = UbuntuChefSoloInitializer.class.getResourceAsStream("/ubuntu-chef-solo-solo.st");

            StringTemplate template = new StringTemplate(new String(ByteStreams.toByteArray(in2)));
            template.setAttribute("recipe_url", recipeUrl);
            Files.write(template.toString().getBytes(), this.soloRbFile);
            in2.close();

        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to create temp file", e);
        }

    }

    @Override
    public Server initialize(final Server server, final String arg, ProvisionedTemplate root)
    {
        try {
            initServer(server, arg);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return server;
    }


    private void initServer(Server server, String nodeJson) throws IOException
    {
        SSH ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalIpAddress());
        try {
            logger.debug("woot, we have an ssh connection, doing stuff!");
            String remote_path = "/home/" + sshUser + "/ubuntu-chef-solo-init.sh";
            ssh.scpUpload(this.chefSoloInitFile, remote_path);
            ssh.exec("chmod +x " + remote_path);

            logger.debug("about to execute chef init script remotely");
            ssh.exec(remote_path);

            File node_json = File.createTempFile("node", "json");
            Files.write(nodeJson, node_json, Charset.forName("UTF-8"));
            ssh.scpUpload(node_json, "/tmp/node.json");
            ssh.exec("sudo mv /tmp/node.json /etc/chef/node.json");

            ssh.scpUpload(soloRbFile, "/tmp/solo.rb");
            ssh.exec("sudo mv /tmp/solo.rb /etc/chef/solo.rb");

            logger.debug("about to execute initial chef-solo");
            ssh.exec("sudo chef-solo");
        }
        finally {
            ssh.close();
        }
    }
}
