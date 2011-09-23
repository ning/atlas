package com.ning.atlas.aws;

import com.ning.atlas.Base;
import com.ning.atlas.Environment;
import com.ning.atlas.Identity;
import com.ning.atlas.Initialization;
import com.ning.atlas.My;
import com.ning.atlas.Server;
import com.ning.atlas.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.ning.atlas.testing.AtlasMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

public class TestRDSProvisioner
{
    private RDSProvisioner rds;

    @Before
    public void setUp() throws Exception
    {
        assumeThat(new File(".awscreds"), exists());

        Properties props = new Properties();
        props.load(new FileInputStream(".awscreds"));
        this.rds = new RDSProvisioner(props.getProperty("aws.access-key"),
                                      props.getProperty("aws.secret-key"));
    }

    @Test
    public void testFoo() throws Exception
    {
        assumeThat(System.getProperty("RUN_EC2_TESTS"), notNullValue());

        Map<String, String> props = new HashMap<String, String>();
        props.put("storage_size", "5");
        props.put("instance_class", "db.m1.small");
        props.put("engine", "MySQL");
        props.put("username", "test");
        props.put("password", "test");

        Base base = new Base("databases",
                             new Environment("ec2"),
                             "rds", Collections.<Initialization>emptyList(),
                             props);

        Server db_server = rds.provision(base, new Node()
        {
            @Override
            public Identity getId()
            {
                return Identity.root();
            }

            @Override
            public String getType()
            {
                return "type";
            }

            @Override
            public String getName()
            {
                return "name";
            }

            @Override
            public My getMy()
            {
                return new My();
            }

            @Override
            public Collection<? extends Node> getChildren()
            {
                return Collections.emptyList();
            }
        });

        System.out.println(db_server);

//        rds.destroy(db_server);
    }
}
