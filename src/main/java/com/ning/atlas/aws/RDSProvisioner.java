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
import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import com.ning.atlas.Thing;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.base.MapConfigSource;
import com.ning.atlas.logging.Logger;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;

import java.util.Map;
import java.util.UUID;

public class RDSProvisioner implements Provisioner
{

    private static final Logger log = Logger.get(RDSProvisioner.class);

    private final AmazonRDSClient rds;

    public RDSProvisioner(String accessKey, String secretKey)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        rds = new AmazonRDSClient(credentials);
    }

    public RDSProvisioner(Map<String, String> attributes)
    {
        this(attributes.get("access_key"), attributes.get("secret_key"));
    }

    @Override
    public Server provision(Base b, Thing node) throws UnableToProvisionServerException
    {
        log.info("Started provisioning %s, this could take a while", node.getId().toExternalForm());
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(b.getAttributes())).build(RDSConfig.class);

        String name = "db-" + UUID.randomUUID().toString();
        CreateDBInstanceRequest req = new CreateDBInstanceRequest(name,
                                                                  cfg.getStorageSize(),
                                                                  cfg.getInstanceClass(),
                                                                  cfg.getEngine(),
                                                                  cfg.getUsername(),
                                                                  cfg.getPassword());
        String license_model = b.getAttributes().containsKey("license_model")
                               ? b.getAttributes().get("license_model")
                               : "general-public-license";

        req.setLicenseModel(license_model);
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
        return new Server(instance.getEndpoint().getAddress(), instance.getEndpoint().getAddress(), attrs);
    }


    public void destroy(Server server)
    {
        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(server.getAttributes().get("instanceId"));
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
         * <code>oracle-se1</code> | <code>oracle-se</code> |
         * <code>oracle-ee</code>
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
