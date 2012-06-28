package com.ning.atlas;

import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Future;

import static com.ning.atlas.spi.protocols.SSHCredentials.defaultCredentials;
import static com.ning.atlas.spi.protocols.SSHCredentials.lookup;

public class AtlasInstaller extends ConcurrentComponent
{
    private final static Logger log = Logger.get(AtlasInstaller.class);
    private final String credentialName;


    public AtlasInstaller(Map<String, String> attributes)
    {
        this.credentialName = attributes.get("credentials");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("populate /etc/atlas with legacy cruft");
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

    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment deployment) throws Exception
    {
        final SSHCredentials creds = lookup(deployment.getSpace(), credentialName)
            .otherwise(defaultCredentials(deployment.getSpace()))
            .otherwise(new IllegalStateException("unable to locate any ssh credentials"));

        // this *always* runs

        boolean success = false;
        while (!success) {
            final Server server = deployment.getSpace()
                .get(host.getId(), Server.class, Missing.NullProperty)
                .getValue();
            final ObjectMapper mapper = makeMapper(deployment.getSpace(), deployment.getEnvironment());
            SSH ssh;
            try {
                ssh = new SSH(creds, server.getExternalAddress());
            }
            catch (IOException e) {
                log.warn(e, "unable to ssh into the server");
                break;

            }
            try {
                ssh.exec("sudo mkdir /etc/atlas");

                // upload the system map
                String sys_map = mapper.writeValueAsString(deployment.getSystemMap().getSingleRoot());
                File sys_map_file = File.createTempFile("system", "map");
                Files.write(sys_map, sys_map_file, Charset.forName("UTF-8"));
                ssh.scpUpload(sys_map_file, "/tmp/system_map.json");
                ssh.exec("sudo mv /tmp/system_map.json /etc/atlas/system_map.json");

                // upload node info
                String node_info = mapper.writeValueAsString(host);
                File node_info_file = File.createTempFile("node", "info");
                Files.write(node_info, node_info_file, Charset.forName("UTF-8"));
                ssh.scpUpload(node_info_file, "/tmp/node_info.json");
                ssh.exec("sudo mv /tmp/node_info.json /etc/atlas/node_info.json");
                success = true;
                return node_info;
            }
            catch (Exception e) {
                log.warn(e, "failed!");
                success = false;
            }
            finally {
                try {
                    ssh.close();
                }
                catch (IOException e) {
                    log.warn("exception closing ssh connection", e);
                }
            }

        }
        return "finished";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        SSH ssh = new SSH(hostId, this.credentialName, d.getSpace());
        try {
            ssh.exec("sudo rm /etc/atlas/node_info.json");
            ssh.exec("sudo rm /etc/atlas/system_map.json");
            return "cleared";
        }
        finally {
            ssh.close();
        }
    }


    public static class HostSerializer extends JsonSerializer<Host>
    {
        private final Space space;
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

        @SuppressWarnings("unchecked")
		@Override
        public void serialize(Host value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            Maybe<Server> s = space.get(value.getId(), Server.class, Missing.RequireAll);
            if (s.isKnown()) {
                String json = space.get(value.getId(), "extra-atlas-attributes").otherwise("{}");
                Map attrs = new ObjectMapper().readValue(json, Map.class);

                jgen.writeObject(new ExtraHost(value, s.getValue(), environment.getProperties(), attrs));
            }
            else {
                jgen.writeObject(value);
            }
        }
    }

    public static class ExtraHost
    {

        private final Host host;
        private final Server server;
        private final Map<String, String> environment;
        @SuppressWarnings("unchecked")
		private final Map attributes;

        private static final ObjectMapper mapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
		@JsonAnyGetter
        public Map<?,?> getProperties() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
        {
            Map<?,?> map = mapper.convertValue(host, Map.class);
            map.remove("children");
            map.putAll(attributes);
            return map;
        }

        @SuppressWarnings("unchecked")
		public ExtraHost(Host host,
                         Server server,
                         Map<String, String> environment,
                         Map attributes)
        {
            this.host = host;
            this.server = server;
            this.environment = environment;
            this.attributes = attributes;
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
