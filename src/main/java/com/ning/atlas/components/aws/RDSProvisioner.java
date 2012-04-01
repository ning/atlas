package com.ning.atlas.components.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.components.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.base.MapConfigSource;
import com.ning.atlas.config.AtlasConfiguration;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.protocols.Database;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.protocols.AWS;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.map.ObjectMapper;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class RDSProvisioner extends ConcurrentComponent
{

    private static final Logger log = Logger.get(RDSProvisioner.class);

    @Override
    public String perform(Host node, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AtlasConfiguration config = AtlasConfiguration.global();
        BasicAWSCredentials creds = new BasicAWSCredentials(config.lookup("aws.key").get(),
                                                            config.lookup("aws.secret").get());

        AmazonRDSClient rds = new AmazonRDSClient(creds);

        Maybe<String> existing_id = d.getSpace().get(node.getId(), "instance-id");
        if (existing_id.isKnown()) {

            DescribeDBInstancesRequest req = new DescribeDBInstancesRequest();
            req.setDBInstanceIdentifier(existing_id.getValue());
            try {
                DescribeDBInstancesResult rs = rds.describeDBInstances(req);
                for (DBInstance instance : rs.getDBInstances()) {
                    if (existing_id.getValue().equals(instance.getDBInstanceIdentifier())
                        && "available".equals(instance.getDBInstanceStatus()))
                    {
                        return "already exists";
                    }
                }
            }
            catch (AmazonServiceException e) {
                // amazon throws an exception if the thing isn't found *sigh*
                if (e.getStatusCode() == 404) {
                    // instance no longer exists, this is excpected
                }
                else {
                    throw new IllegalStateException("unexpected error talking to RDS Api", e);
                }
            }
        }

        log.info("Started provisioning %s, this could take a while", node.getId().toExternalForm());
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(uri.getParams())).build(RDSConfig.class);

        String name = "db-" + UUID.randomUUID().toString();
        CreateDBInstanceRequest req = new CreateDBInstanceRequest(name,
                                                                  cfg.getStorageSize(),
                                                                  cfg.getInstanceClass(),
                                                                  cfg.getEngine(),
                                                                  cfg.getUsername(),
                                                                  cfg.getPassword());
        String license_model = uri.getParams().containsKey("license_model")
                               ? uri.getParams().get("license_model")
                               : "general-public-license";

        req.setLicenseModel(license_model);

        Maybe<String> db_name = Maybe.elideNull(uri.getParams().get("name"));
        if (db_name.isKnown()) {
            req.setDBName(db_name.getValue());
        }
        Maybe<String> sec_group = Maybe.elideNull(uri.getParams().get("security_group"));
        if (sec_group.isKnown()) {
            AWS.waitForRDSSecurityGroup(sec_group.getValue(), d.getSpace(), 1, TimeUnit.MINUTES);
            req.setDBSecurityGroups(asList(sec_group.getValue()));
        }

        DBInstance db = rds.createDBInstance(req);

        DBInstance instance = null;
        String last_state = "";
        do {
            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw Throwables.propagate(e);
            }
            DescribeDBInstancesRequest rdy = new DescribeDBInstancesRequest();
            rdy.setDBInstanceIdentifier(db.getDBInstanceIdentifier());
            DescribeDBInstancesResult rs;
            try {
                rs = rds.describeDBInstances(rdy);
            }
            catch (AmazonServiceException e) {
                continue;
            }
            instance = rs.getDBInstances().get(0);
            String state = instance.getDBInstanceStatus();
            if (!last_state.equals(state)) {
                log.debug("database instance %s achieved state %s", instance.getDBInstanceIdentifier(), state);
                last_state = state;
            }
        }
        while (!(instance != null
                 && instance.getDBInstanceStatus().equals("available")
                 && instance.getEndpoint() != null));


        Database database = new Database();
        database.setHost(instance.getEndpoint().getAddress());
        database.setPort(instance.getEndpoint().getPort());
        database.setPassword(cfg.getPassword());
        database.setUsername(cfg.getUsername());
        database.setName(instance.getDBName() == null ? "" : instance.getDBName());
        d.getSpace().store(node.getId(), database);

        Map<String, String> attrs = Maps.newHashMap();
        attrs.put("port", instance.getEndpoint().getPort().toString());
        attrs.put("instanceId", instance.getDBInstanceIdentifier());
        attrs.put("instanceClass", instance.getDBInstanceClass());
        attrs.put("name", instance.getDBName() == null ? "" : instance.getDBName());
        attrs.put("engine", instance.getEngine());
        attrs.put("engineVersion", instance.getEngineVersion());
        attrs.put("password", cfg.getPassword());
        attrs.put("username", cfg.getUsername());
        attrs.put("storageSize", String.valueOf(cfg.getStorageSize()));
        attrs.put("host", instance.getEndpoint().getAddress());

        log.info("Finished provisioning %s", node.getId().toExternalForm());

        // TODO save to space somehow

        d.getSpace().store(node.getId(), new Server(instance.getEndpoint().getAddress()));

        d.getSpace().store(node.getId(), "instance-id", instance.getDBInstanceIdentifier());

        d.getSpace().store(node.getId(), "extra-atlas-attributes", new ObjectMapper().writeValueAsString(attrs));

        return "cool beans";
    }

    @Override
    public String unwind(Identity hostId, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        AWS.Credentials creds = d.getSpace().get(AWS.ID, AWS.Credentials.class, Missing.RequireAll)
                                 .otherwise(new IllegalStateException("AWS credentials are not available"));

        AmazonRDSClient rds = new AmazonRDSClient(new BasicAWSCredentials(creds.getAccessKey(), creds.getSecretKey()));

        String mid = d.getSpace().get(hostId, "instance-id")
                      .otherwise(new IllegalStateException("No instance id found, cannot unwind"));

        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(mid);
        req.setSkipFinalSnapshot(true);
        rds.deleteDBInstance(req);
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

        AmazonRDSClient rds = new AmazonRDSClient(new BasicAWSCredentials(creds.getAccessKey(), creds.getSecretKey()));

        String instance_id = d.getSpace().get(id, "instance-id").getValue();
        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(instance_id);
        req.setSkipFinalSnapshot(true);
        rds.deleteDBInstance(req);
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
