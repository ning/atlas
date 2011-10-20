package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class AtlasInstaller extends BaseComponent implements Installer
{
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

    ObjectMapper makeMapper(Space space, Environment environment)
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        SimpleModule module = new SimpleModule("host-thing", new Version(1, 0, 0, ""));
        module.addSerializer(new HostSerializer(space, environment));
        mapper.registerModule(module);
        return mapper;
    }

    /**
     * Generates a JSON string which is the system map
     */
    String generateSystemMap(ObjectMapper mapper, SystemMap map) throws IOException
    {
        return mapper.writeValueAsString(map.getSingleRoot());
    }


    public static class HostSerializer extends JsonSerializer<Host>
    {
        private final Space       space;
        private final Environment environment;

        HostSerializer(Space space, Environment environment)
        {
            this.space = space;
            this.environment = environment;
        }

        @Override
        public Class<Host> handledType()
        {
            return Host.class;
        }

        @Override
        public void serialize(Host value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            Maybe<Server> s = space.get(value.getId(), Server.class, Missing.RequireAll);
            if (s.isKnown()) {
                jgen.writeObject(new ExtraHost(value, s.getValue(), environment.getProperties()));
            }
        }
    }

    public static class ExtraHost
    {

        private final Host                host;
        private final Server              server;
        private final Map<String, String> environment;

        private static final ObjectMapper mapper = new ObjectMapper();

        @JsonAnyGetter
        public Map getProperties() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
        {
            Map map = mapper.convertValue(host, Map.class);
            map.remove("children");
            return map;
        }

        public ExtraHost(Host host, Server server, Map<String, String> environment)
        {
            this.host = host;
            this.server = server;
            this.environment = environment;
        }

        public Map<String, String> getEnvironment()
        {
            return environment;
        }

        public Server getServer()
        {
            return server;
        }
    }
}
