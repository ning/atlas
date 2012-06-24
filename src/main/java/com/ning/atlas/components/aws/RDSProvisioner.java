package com.ning.atlas.components.aws;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.rds.RDSApi;
import org.jclouds.rds.domain.Instance;
import org.jclouds.rds.domain.InstanceRequest;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.Host;
import com.ning.atlas.base.MapConfigSource;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.AWS.Credentials;
import com.ning.atlas.spi.protocols.Database;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.space.Missing;

public class RDSProvisioner extends ConcurrentComponent
{

    private static final Logger log = Logger.get(RDSProvisioner.class);

    @Override
    public String perform(Host node, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AtlasConfiguration config = AtlasConfiguration.global();
        Credentials creds = new AWS.Credentials();
        creds.setAccessKey(config.lookup("aws.key").get());
        creds.setSecretKey(config.lookup("aws.secret").get());
        
        final RDSApi rdsApi = AWS.rdsApi(creds);

        Maybe<String> existing_id = d.getSpace().get(node.getId(), "instance-id");
        if (existing_id.isKnown()) {
           Instance instance = rdsApi.getInstanceApi().get(existing_id.getValue());
            if (instance != null
                    && !Predicates.in(ImmutableSet.of(Instance.Status.DELETING, Instance.Status.FAILED)).apply(
                            instance.getStatus()))
                return "already exists";
        }

        log.info("Started provisioning %s, this could take a while", node.getId().toExternalForm());
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(uri.getParams())).build(RDSConfig.class);

        String name = "db-" + UUID.randomUUID().toString();
        
        InstanceRequest.Builder<?> requestBuilder = InstanceRequest.builder()
                                                                   .instanceClass(cfg.getInstanceClass())
                                                                   .allocatedStorageGB(cfg.getStorageSize())
                                                                   .engine(cfg.getEngine())
                                                                   .masterUsername(cfg.getUsername())
                                                                   .masterPassword(cfg.getPassword());
        String license_model = uri.getParams().containsKey("license_model")
                ? uri.getParams().get("license_model")
                : "general-public-license";
        requestBuilder.licenseModel(license_model);

        Maybe<String> db_name = Maybe.elideNull(uri.getParams().get("name"));
        if (db_name.isKnown()) {
            requestBuilder.name(db_name.getValue());
        }
        
        Maybe<String> sec_group = Maybe.elideNull(uri.getParams().get("security_group"));
        if (sec_group.isKnown()) {
            AWS.waitForRDSSecurityGroup(sec_group.getValue(), d.getSpace(), 1, TimeUnit.MINUTES);
            requestBuilder.securityGroup(sec_group.getValue());
        }
        
        Instance newInstance = rdsApi.getInstanceApi().create(name, requestBuilder.build());

        RetryablePredicate<Instance> instanceAvailable = new RetryablePredicate<Instance>(new Predicate<Instance>() {

            @Override
            public boolean apply(Instance input) {
                Instance refreshed = rdsApi.getInstanceApi().get(input.getId());
                return refreshed.getStatus() == Instance.Status.AVAILABLE && refreshed.getEndpoint().isPresent();
            }

         }, 600, 1, 1, TimeUnit.SECONDS);

        if (!instanceAvailable.apply(newInstance))
            throw new IllegalStateException("instance never hit state AVAILABLE!: "+ newInstance);

        Instance instance = rdsApi.getInstanceApi().get(newInstance.getId());

        Database database = new Database();
        database.setHost(instance.getEndpoint().get().getHostText());
        database.setPort(instance.getEndpoint().get().getPort());
        database.setPassword(cfg.getPassword());
        database.setUsername(cfg.getUsername());
        database.setName(instance.getName().or(""));
        d.getSpace().store(node.getId(), database);

        Map<String, String> attrs = Maps.newHashMap();
        attrs.put("port", instance.getEndpoint().get().getPort() + "");
        attrs.put("instanceId", instance.getId());
        attrs.put("instanceClass", instance.getInstanceClass());
        attrs.put("name", instance.getName().or(""));
        attrs.put("engine", instance.getEngine());
        attrs.put("engineVersion", instance.getEngineVersion());
        attrs.put("password", cfg.getPassword());
        attrs.put("username", cfg.getUsername());
        attrs.put("storageSize", String.valueOf(cfg.getStorageSize()));
        attrs.put("host", instance.getEndpoint().get().getHostText());

        log.info("Finished provisioning %s", node.getId().toExternalForm());

        // TODO save to space somehow

        d.getSpace().store(node.getId(), new Server(instance.getEndpoint().get().getHostText()));

        d.getSpace().store(node.getId(), "instance-id", instance.getId());

        d.getSpace().store(node.getId(), "extra-atlas-attributes", new ObjectMapper().writeValueAsString(attrs));

        return "cool beans";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                 .otherwise(new IllegalStateException("AWS credentials are not available"));

        creds.setAccessKey(creds.getAccessKey());
        creds.setSecretKey(creds.getSecretKey());
        
        RDSApi rdsApi = AWS.rdsApi(creds);

        String mid = d.getSpace().get(hostId, "instance-id")
                      .otherwise(new IllegalStateException("No instance id found, cannot unwind"));

        rdsApi.getInstanceApi().delete(mid);
        return "cleared";
    }

    @Override
    public Future<String> describe(Host server,
                                   Uri<? extends Component> uri,
                                   Deployment deployment)
    {
        return Futures.immediateFuture("provision an rds database");
    }

    public void destroy(Identity id, Deployment d)
    {
        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                 .otherwise(new IllegalStateException("AWS credentials are not available"));

        creds.setAccessKey(creds.getAccessKey());
        creds.setSecretKey(creds.getSecretKey());
        
        RDSApi rdsApi = AWS.rdsApi(creds);

        String instance_id = d.getSpace().get(id, "instance-id").getValue();

        rdsApi.getInstanceApi().delete(instance_id);
    }

    public interface RDSConfig
    {
        /**
         * The amount of storage (in gigabytes) to be
         * initially allocated for the database instance. Must be an integer from
         * 5 to 1024.
         */
        @Config("storage_size")
        public abstract int getStorageSize();

        /**
         * The compute and memory capacity of the DB
         * Instance. <p> Valid Values: <code>db.m1.small | db.m1.large |
         * db.m1.xlarge | db.m2.xlarge |db.m2.2xlarge | db.m2.4xlarge</code>
         */
        @Config("instance_class")
        public abstract String getInstanceClass();

        /**
         * The name of the database engine to be used for this
         * instance. <p> Valid Values: <code>MySQL</code> |
         * <code>databases-se1</code> | <code>databases-se</code> |
         * <code>databases-ee</code>
         */
        @Config("engine")
        public abstract String getEngine();

        /**
         * The name of master user for the client DB
         * Instance. <p>Constraints: <ul> <li>Must be 1 to 16 alphanumeric
         * characters.</li> <li>First character must be a letter.</li> <li>Cannot
         * be a reserved word for the chosen database engine.</li> </ul>
         */
        @Config("username")
        public abstract String getUsername();

        /**
         * The password for the master DB Instance
         * user. <p> Constraints: Must contain 4 to 41 alphanumeric characters.
         */
        @Config("password")
        public abstract String getPassword();
    }
}
