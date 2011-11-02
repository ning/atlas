package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Space;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestInformationGatheringListener
{
    @org.junit.Test
    public void testFoo() throws Exception
    {
        Space space = InMemorySpace.newInstance();
        Environment e = new Environment();
        SystemMap map = new SystemMap();
        ActualDeployment d = new ActualDeployment(map, e, space);

        Map<String, String> qs = ImmutableMap.of("aws.secret_key", "What is your AWS secret key? ",
                                                 "aws.access_key", "What is your AWS access key? ",
                                                 "aws.keypair_id", "Full path to ssh key file: ");
        InformationGatheringListener listener = new InformationGatheringListener(qs);

        File atlasrc = new File(".atlasrc");
        Files.write("aws:\n" +
                    "  secret_key: 123\n" +
                    "  access_key: abc\n" +
                    "  keypair_id: waffle-hut\n", atlasrc, Charset.forName("UTF8"));

        listener.startDeployment(d).get();

        assertThat(space.get("aws.secret_key").getValue(), equalTo("123"));
        assertThat(space.get("aws.access_key").getValue(), equalTo("abc"));
        assertThat(space.get("aws.keypair_id").getValue(), equalTo("waffle-hut"));
    }
}
