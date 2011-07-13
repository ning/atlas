package com.ning.atlas;

import com.google.common.io.Files;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class AtlasInitializer implements Initializer
{
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    private final Logger log = LoggerFactory.getLogger(AtlasInitializer.class);
    private final String sshUser;
    private final String sshKeyFile;

    public AtlasInitializer(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");
    }

    @Override
    public Server initialize(Server server, String arg, ProvisionedTemplate root, ProvisionedServerTemplate node) throws Exception
    {
        SSH ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalAddress());
        try {
            log.debug("initializing {}", server.getExternalAddress());

            ssh.exec("sudo mkdir /etc/atlas");

            // upload the system map
            String sys_map = mapper.writeValueAsString(root);
            File sys_map_file = File.createTempFile("system", "map");
            Files.write(sys_map, sys_map_file, Charset.forName("UTF-8"));
            ssh.scpUpload(sys_map_file, "/tmp/system_map.json");
            ssh.exec("sudo mv /tmp/system_map.json /etc/atlas/system_map.json");

            // upload node info
            String node_info = mapper.writeValueAsString(node);
            File node_info_file = File.createTempFile("node", "info");
            Files.write(node_info, node_info_file, Charset.forName("UTF-8"));
            ssh.scpUpload(node_info_file, "/tmp/node_info.json");
            ssh.exec("sudo mv /tmp/node_info.json /etc/atlas/node_info.json");
        }
        finally {
            ssh.close();
        }
        return server;
    }
}
