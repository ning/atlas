package com.ning.atlas.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.google.common.base.Objects;
import com.ning.atlas.Base;
import com.ning.atlas.Provisioner;
import com.ning.atlas.Server;
import com.ning.atlas.UnableToProvisionServerException;
import com.ning.atlas.base.MapConfigSource;
import org.skife.config.Config;
import org.skife.config.ConfigurationObjectFactory;

import java.util.Map;

public class RDSProvisioner implements Provisioner
{
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
    public RDSServer provision(Base b) throws UnableToProvisionServerException
    {
        RDSConfig cfg = new ConfigurationObjectFactory(new MapConfigSource(b.getAttributes())).build(RDSConfig.class);

        CreateDBInstanceRequest req = new CreateDBInstanceRequest(cfg.getInstanceId(),
                                                                  cfg.getStorageSize(),
                                                                  cfg.getInstanceClass(),
                                                                  cfg.getEngine(),
                                                                  cfg.getUsername(),
                                                                  cfg.getPassword());

        DBInstance db = rds.createDBInstance(req);

        DBInstance instance;
        do {
            DescribeDBInstancesRequest rdy = new DescribeDBInstancesRequest();
            rdy.setDBInstanceIdentifier(db.getDBInstanceIdentifier());
            DescribeDBInstancesResult rs = rds.describeDBInstances(rdy);
            instance = rs.getDBInstances().get(0);
        }
        while (!(instance.getDBInstanceStatus().equals("available") && instance.getEndpoint() != null));

        return new RDSServer(instance.getEndpoint().getAddress(),
                             instance.getEndpoint().getPort(),
                             instance.getDBInstanceIdentifier(),
                             b);
    }


    public void destroy(RDSServer server)
    {
        DeleteDBInstanceRequest req = new DeleteDBInstanceRequest(server.getInstanceId());
        rds.deleteDBInstance(req);
    }

    public static class RDSServer extends Server
    {
        private final Integer port;
        private final String  instanceId;

        public RDSServer(String ip, Integer port, String instanceId, Base base)
        {
            super(ip, ip, base);
            this.port = port;
            this.instanceId = instanceId;
        }

        public Integer getPort()
        {
            return port;
        }

        public String getInstanceId()
        {
            return instanceId;
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this).toString();
        }
    }

    public interface RDSConfig
    {
        /**
         * The DB Instance identifier. This parameter
         * is stored as a lowercase string. <p>Constraints: <ul> <li>Must contain
         * from 1 to 63 alphanumeric characters or hyphens.</li> <li>First
         * character must be a letter.</li> <li>Cannot end with a hyphen or
         * contain two consecutive hyphens.</li> </ul> <p>Example:
         * <code>mydbinstance</code>
         */
        @Config("instance_id")
        public abstract String getInstanceId();

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
