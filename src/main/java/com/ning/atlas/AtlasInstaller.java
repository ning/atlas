package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class AtlasInstaller extends BaseComponent implements Installer
{
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    private final Logger log = LoggerFactory.getLogger(AtlasInstaller.class);
    private final String sshUser;
    private final String sshKeyFile;

    public AtlasInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");
    }

//    @Override
//    public void install(Server server, String arg, Node root, Node node) throws Exception
//    {
//        SSH ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalAddress());
//        try {
//            log.info("initializing {} to become a {}", server.getExternalAddress(), node.getType());
//
//            ssh.exec("sudo mkdir /etc/atlas");
//
//            // upload the system map
//            String sys_map = mapper.writeValueAsString(root);
//            File sys_map_file = File.createTempFile("system", "map");
//            Files.write(sys_map, sys_map_file, Charset.forName("UTF-8"));
//            ssh.scpUpload(sys_map_file, "/tmp/system_map.json");
//            ssh.exec("sudo mv /tmp/system_map.json /etc/atlas/system_map.json");
//
//            // upload node info
//            String node_info = mapper.writeValueAsString(node);
//            File node_info_file = File.createTempFile("node", "info");
//            Files.write(node_info, node_info_file, Charset.forName("UTF-8"));
//            ssh.scpUpload(node_info_file, "/tmp/node_info.json");
//            ssh.exec("sudo mv /tmp/node_info.json /etc/atlas/node_info.json");
//        }
//        finally {
//            ssh.close();
//        }
//    }

    @Override
    public Future<String> describe(Host server, Uri<Installer> uri, Deployment deployment)
    {
        return Futures.immediateFuture("populate /etc/atlas with legacy cruft");
    }

    @Override
    public Future<?> install(Host server, Uri<Installer> uri, Deployment deployment)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
