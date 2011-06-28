package com.ning.atlas.chef;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.ning.atlas.Initializer;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.SSH;
import com.ning.atlas.Server;
import org.antlr.stringtemplate.StringTemplate;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

public class UbuntuChefSoloInitializer implements Initializer
{
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    private final static Logger logger = LoggerFactory.getLogger(UbuntuChefSoloInitializer.class);

    private final String sshUser;
    private final String sshKeyFile;
    private final File   chefSoloInitFile;
    private final File   soloRbFile;

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
    public Server initialize(final Server server, final String arg, ProvisionedTemplate root) throws Exception
    {
        boolean done = true;
        do {
            String sys_map = mapper.writeValueAsString(root);
            File sys_map_file = File.createTempFile("system", "map");
            Files.write(sys_map, sys_map_file, Charset.forName("UTF-8"));
            initServer(server, createNodeJsonFor(arg), sys_map_file);
            sys_map_file.delete();
        }
        while (!done);
        return server;
    }


    private void initServer(Server server, String nodeJson, File sysMapFile) throws IOException
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

            ssh.exec("sudo mkdir /etc/atlas");

            ssh.scpUpload(sysMapFile, "/tmp/system_map.json");
            ssh.exec("sudo mv /tmp/system_map.json /etc/atlas/system_map.json");

            logger.debug("about to execute initial chef-solo");
            ssh.exec("sudo chef-solo");
        }
        finally {
            ssh.close();
        }
    }

//    private final ObjectMapper mapper = new ObjectMapper();

    public String createNodeJsonFor(String literal)
    {
        if (literal.contains("run_list")) {
            return literal;
        }
        else {
            Node node = new Node();
            Iterable<String> split = Splitter.on(Pattern.compile(",\\s*")).split(literal);
            Iterables.addAll(node.run_list, split);
            try {
                return mapper.writeValueAsString(node);
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static final class Node
    {
        public List<String> run_list = Lists.newArrayList();

        public Node(String... elems)
        {
            run_list.addAll(asList(elems));
        }

        public Node()
        {
            this(new String[]{});
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return !(run_list != null ? !run_list.equals(node.run_list) : node.run_list != null);

        }

        @Override
        public int hashCode()
        {
            return run_list != null ? run_list.hashCode() : 0;
        }
    }
}
