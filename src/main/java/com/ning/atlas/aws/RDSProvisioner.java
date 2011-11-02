package com.ning.atlas.aws;

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
import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.base.MapConfigSource;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.protocols.Server;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.map.ObjectMapper;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class RDSProvisioner extends ConcurrentComponent<String>
{

    private static final Logger log = Logger.get(RDSProvisioner.class);

    private final AtomicReference<AmazonRDSClient> rds = new AtomicReference<AmazonRDSClient>();

    public RDSProvisioner(String accessKey, String secretKey)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        rds.set(new AmazonRDSClient(credentials));
    }

    public RDSProvisioner()
    {
        // for jruby
    }

    @Override
    protected void startLocal(Deployment deployment)
    {
        Space s = deployment.getSpace();
        BasicAWSCredentials credentials = new BasicAWSCredentials(s.get("atlas.aws.access_key").getValue(),
                                                                  s.get("atlas.aws.secret_key").getValue());
        rds.set(new AmazonRDSClient(credentials));

    }

    @Override
    public String perform(Host node, Uri<? extends Component> uri, Deployment d) throws Exception
    {

        if (d.getSpace().get(node.getId(), "instance-id").isKnown()) {
            return "already exists";
        }

        log.info("Started provisioning %s, this could take a while", node.getId().toExternalForm());
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(uri.getParamsSimple())).build(RDSConfig.class);

        String name = "db-" + UUID.randomUUID().toString();
        CreateDBInstanceRequest req = new CreateDBInstanceRequest(name,
                                                                  cfg.getStorageSize(),
                                                                  cfg.getInstanceClass(),
                                                                  cfg.getEngine(),
                                                                  cfg.getUsername(),
                                                                  cfg.getPassword());
        String license_model = uri.getParamsSimple().containsKey("license_model")
                               ? uri.getParamsSimple().get("license_model")
                               : "general-public-license";

        req.setLicenseModel(license_model);
        DBInstance db = rds.get().createDBInstance(req);

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
                rs = rds.get().describeDBInstances(rdy);
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

        log.info("Finished provisioning %s", node.getId().toExternalForm());

        // TODO save to space somehow

        d.getSpace().store(node.getId(), new Server(instance.getEndpoint().getAddress()));

        d.getSpace().store(node.getId(), "instance-id", instance.getDBInstanceIdentifier());

        d.getSpace().store(node.getId(), "extra-atlas-attributes", new ObjectMapper().writeValueAsString(attrs));

        return "cool beans";
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
        String instance_id = d.getSpace().get(id, "instance-id").getValue();
        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(instance_id);
        req.setSkipFinalSnapshot(true);
        rds.get().deleteDBInstance(req);
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
