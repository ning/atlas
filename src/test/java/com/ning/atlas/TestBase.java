package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import javax.naming.ldap.InitialLdapContext;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestBase
{
    @Test
    public void testFoo() throws Exception
    {
        final List<String> inits = Lists.newArrayList();
        Environment env = new Environment("unit-test");
        env.addInitializer("waffle", new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg)
            {
                inits.add("waffle+" + arg);
                return server;
            }
        });

        env.addInitializer("pancake", new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg)
            {
                inits.add("pancake+" + arg);
                return server;
            }
        });

        Base base = new Base("test", env);
        base.addInit("waffle:hut");
        base.addInit("pancake:house");

        base.initialize(new Server()
        {
            @Override
            public String getExternalIpAddress()
            {
                return null;
            }

            @Override
            public String getInternalIpAddress()
            {
                return null;
            }

            @Override
            public Server initialize()
            {
                return this;
            }
        });

        assertThat(inits, equalTo(asList("waffle+hut", "pancake+house")));


    }


}
