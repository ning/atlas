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
import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.base.MapConfigSource;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class RDSProvisioner implements Provisioner
{

    private final Logger log = LoggerFactory.getLogger(RDSProvisioner.class);

    private final AmazonRDSClient rds;
    private String licenseModel;

    public RDSProvisioner(String accessKey, String secretKey, String licenseModel)
    {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        rds = new AmazonRDSClient(credentials);
        this.licenseModel = licenseModel;
    }

    public RDSProvisioner(String accessKey, String secretKey)
    {
        this(accessKey, secretKey, "general-public-license");
    }

    public RDSProvisioner(Map<String, String> attributes)
    {
        this(attributes.get("access_key"), attributes.get("secret_key"), attributes.get("license_model"));
    }

    @Override
    public RDSServer provision(Base b) throws UnableToProvisionServerException
    {
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(b.getAttributes())).build(RDSConfig.class);

        CreateDBInstanceRequest req = new CreateDBInstanceRequest("db-" + UUID.randomUUID().toString(),
                                                                  cfg.getStorageSize(),
                                                                  cfg.getInstanceClass(),
                                                                  cfg.getEngine(),
                                                                  cfg.getUsername(),
                                                                  cfg.getPassword());
        req.setLicenseModel(licenseModel);
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
                log.debug("database instance {} achieved state {}", instance.getDBInstanceIdentifier(), state);
                last_state = state;
            }
        }
        while (!(instance != null
                 && instance.getDBInstanceStatus().equals("available")
                 && instance.getEndpoint() != null));

        return new RDSServer(instance.getEndpoint().getAddress(),
                             instance.getEndpoint().getPort(),
                             instance.getDBInstanceIdentifier(),
                             cfg,
                             b);
    }


    public void destroy(RDSServer server)
    {
        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(server.getInstanceId());
        req.setSkipFinalSnapshot(true);
        rds.deleteDBInstance(req);
    }

    public static class RDSServer extends Server
    {
        private final Integer port;
        private final String  instanceId;
        private final String  engine;
        private final String  instanceClass;
        private final String password;
        private final int storageSize;
        private final String username;

        public RDSServer(String ip,
                         Integer port,
                         String instanceId,
                         RDSConfig config,
                         Base base)
        {
            super(ip, ip, base);
            this.port = port;
            this.instanceId = instanceId;
            this.engine = config.getEngine();
            this.instanceClass = config.getInstanceClass();
            this.password = config.getPassword();
            this.storageSize = config.getStorageSize();
            this.username = config.getUsername();
        }

        public Integer getPort()
        {
            return port;
        }

        public String getInstanceId()
        {
            return instanceId;
        }

        public String getEngine()
        {
            return engine;
        }

        public String getInstanceClass()
        {
            return instanceClass;
        }

        public String getPassword()
        {
            return password;
        }

        public int getStorageSize()
        {
            return storageSize;
        }

        public String getUsername()
        {
            return username;
        }
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
