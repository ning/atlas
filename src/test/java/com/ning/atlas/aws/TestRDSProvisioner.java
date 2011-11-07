package com.ning.atlas.aws;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ning.atlas.ActualDeployment;
import com.ning.atlas.Element;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.protocols.AWS;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.ning.atlas.aws.TestEC2Provisioner.isAvailable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestRDSProvisioner
{
    @Test
    public void testFoo() throws Exception
    {
        assumeThat("ec2", isAvailable());

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("storage_size", "5");
        params.put("instance_class", "db.m1.small");
        params.put("engine", "MySQL");
        params.put("username", "test");
        params.put("password", "test");
        Uri<Provisioner> uri = Uri.valueOf("rds:testdb", params.asMap());

        Space space = InMemorySpace.newInstance();
        AWSConfig cfg = EC2Helper.loadAwsConfig();

        AWS.Credentials creds = new AWS.Credentials();
        creds.setAccessKey(cfg.getAccessKey());
        creds.setSecretKey(cfg.getSecretKey());
        space.store(AWS.ID, creds);


        RDSProvisioner p = new RDSProvisioner();

        Identity id = Identity.root().createChild("db", "0");
        Host db = new Host(id, "db", new My(), Collections.<Uri<Installer>>emptyList());
        SystemMap map = new SystemMap(Arrays.<Element>asList(db));

        Environment e = new Environment();
        Deployment d = new ActualDeployment(map, e, space);

        p.start(d);

        Status s = p.provision(db, uri, d).get();

        System.out.println(s);

        p.destroy(id, d);
    }

    @Test
    public void testJacksonBehavior() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> m1 = Maps.newHashMap();
        m1.put("hello", "world");
        m1.put("stuf", Arrays.asList(1,2,3));
        String json = mapper.writeValueAsString(m1);

        Map<String, Object> m2 = mapper.readValue(json, Map.class);
        assertThat(m1, equalTo(m2));
    }

    @Test
    public void testJsonSerializationOfSystemMapWithRdsItemInIt() throws Exception
    {

    }
}
