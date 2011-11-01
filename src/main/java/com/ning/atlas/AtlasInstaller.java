package com.ning.atlas;

import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.protocols.Server;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

public class AtlasInstaller extends BaseComponent implements Installer
{
    private final static Logger log = Logger.get(AtlasInstaller.class);

    private final ExecutorService es = Executors.newCachedThreadPool();
    private final String sshUser;
    private final String sshKeyFile;

    public AtlasInstaller(Map<String, String> attributes)
    {
        this.sshUser = attributes.get("ssh_user");
        checkNotNull(sshUser, "ssh_user attribute required");

        this.sshKeyFile = attributes.get("ssh_key_file");
        checkNotNull(sshKeyFile, "ssh_key_file attribute required");
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return Futures.immediateFuture("populate /etc/atlas with legacy cruft");
    }

    @Override
    public Future<String> install(final Host host, Uri<Installer> uri, final Deployment deployment)
    {
        // this *always* runs
        return es.submit(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                boolean success = false;
                while (!success) {
                    final Server server = deployment.getSpace()
                                                    .get(host.getId(), Server.class, Missing.NullProperty)
                                                    .getValue();
                    final ObjectMapper mapper = makeMapper(deployment.getSpace(), deployment.getEnvironment());
                    SSH ssh;
                    try {
                        ssh = new SSH(new File(sshKeyFile), sshUser, server.getExternalAddress());
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
        });
    }

    @Override
    protected void finishLocal(Deployment deployment)
    {
        es.shutdown();
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

        private final Host                host;
        private final Server              server;
        private final Map<String, String> environment;
        private final Map                 attributes;

        private static final ObjectMapper mapper = new ObjectMapper();

        @JsonAnyGetter
        public Map getProperties() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
        {
            Map map = mapper.convertValue(host, Map.class);
            map.remove("children");
            map.putAll(attributes);
            return map;
        }

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
