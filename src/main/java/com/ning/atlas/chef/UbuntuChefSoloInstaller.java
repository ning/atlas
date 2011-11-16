package com.ning.atlas.chef;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.SSH;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.SSHCredentials;
import com.ning.atlas.spi.protocols.Server;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ning.atlas.spi.protocols.SSHCredentials.defaultCredentials;
import static com.ning.atlas.spi.protocols.SSHCredentials.lookup;
import static java.util.Arrays.asList;

public class UbuntuChefSoloInstaller extends ConcurrentComponent
{
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    private final static Logger logger = LoggerFactory.getLogger(UbuntuChefSoloInstaller.class);

    private final File   chefSoloInitFile;
    private final File   soloRbFile;
    private final File   s3InitFile;
    private final String credentialName;

    public UbuntuChefSoloInstaller(Map<String, String> attributes)
    {
        final String recipeUrl = attributes.get("recipe_url");
        checkNotNull(recipeUrl, "recipe_url attribute required");

        this.credentialName = attributes.get("credentials");

        Maybe<String> s3AccessKey = Maybe.elideNull(attributes.get("s3_access_key"));
        Maybe<String> s3SecretKey = Maybe.elideNull(attributes.get("s3_secret_key"));

        try {
            this.chefSoloInitFile = File.createTempFile("chef-solo-init", "sh");
            InputStream in = UbuntuChefSoloInstaller.class.getResourceAsStream("/ubuntu-chef-solo-setup.sh");
            Files.write(ByteStreams.toByteArray(in), this.chefSoloInitFile);
            in.close();

            this.soloRbFile = File.createTempFile("solo", "rb");
            InputStream in2 = UbuntuChefSoloInstaller.class.getResourceAsStream("/ubuntu-chef-solo-solo.st");
            StringTemplate template = new StringTemplate(new String(ByteStreams.toByteArray(in2)));
            template.setAttribute("recipe_url", recipeUrl);
            Files.write(template.toString().getBytes(), this.soloRbFile);
            in2.close();

            this.s3InitFile = File.createTempFile("s3_init", ".rb");
            if (s3AccessKey.isKnown() && s3SecretKey.isKnown()) {
                InputStream in3 = UbuntuChefSoloInstaller.class.getResourceAsStream("/s3_init.rb.st");
                StringTemplate s3_init_template = new StringTemplate(new String(ByteStreams.toByteArray(in3)));
                s3_init_template.setAttribute("aws_access_key", s3AccessKey.otherwise(""));
                s3_init_template.setAttribute("aws_secret_key", s3SecretKey.otherwise(""));
                Files.write(s3_init_template.toString().getBytes(), this.s3InitFile);
            }
            else {
                Files.write("".getBytes(), this.s3InitFile);
            }

        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to create temp file", e);
        }

    }

    @Override
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture("install chef solo and assign it <roles>");
    }


    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        return initServer(host, createNodeJsonFor(uri.getFragment()), d);
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        throw new IllegalStateException("No unwinding chef stuff for now");
    }

    private String initServer(Host host, String nodeJson, Deployment d) throws IOException
    {
        final SSHCredentials creds = lookup(d.getSpace(), credentialName)
                    .otherwise(defaultCredentials(d.getSpace()))
                    .otherwise(new IllegalStateException("unable to locate any ssh credentials"));


        Server server = d.getSpace().get(host.getId(), Server.class, Missing.RequireAll).getValue();
        SSH ssh = new SSH(creds, server.getExternalAddress());
        try {
            String remote_path = "/home/" + creds.getUserName() + "/ubuntu-chef-solo-init.sh";
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

            ssh.scpUpload(s3InitFile, "/tmp/s3_init.rb");
            ssh.exec("sudo mv /tmp/s3_init.rb /etc/chef/s3_init.rb");

            logger.debug("about to execute initial chef-solo");
            return ssh.exec("sudo chef-solo");
        }
        finally {
            ssh.close();
        }
    }

    public String createNodeJsonFor(String literal)
    {
        final Node node;
        try {
            if (literal.contains("run_list")) {
                node = mapper.readValue(literal, Node.class);
            }
            else {
                node = new Node();
                Iterable<String> split = Splitter.on(Pattern.compile(",\\s*")).split(literal);
                Iterables.addAll(node.run_list, split);
            }
            return mapper.writeValueAsString(node);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
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
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
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
