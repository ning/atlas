package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestInitialization
{
    @Test
    public void testInitialize() throws Exception
    {

        final AtomicBoolean initialized = new AtomicBoolean(false);
        Initializer initializer = new Initializer()
        {
            @Override
            public Server initialize(Server server, String arg, ProvisionedElement root,
                      ProvisionedServer node)
            {
                initialized.set(true);
                assertThat(arg, equalTo("meow"));
                return server;
            }
        };

        Environment env = new Environment("env");
        env.addInitializer("woof", initializer);

        Base base = new Base("server", env, "fake", ImmutableList.<Initialization>of(Initialization.parseUriForm("woof:meow")), Collections.<String, String>emptyMap());

        List<? extends ProvisionedElement> children = asList(
            new ProvisionedServer("server", "0", new My(),
                                          new Server("10.0.0.1", "10.0.0.1"), Collections.<String>emptyList(), base));

        ProvisionedSystem root = new ProvisionedSystem("root", "0", new My(), children);
        InitializedTemplate initialized_root = root.initialize(MoreExecutors.sameThreadExecutor()).get();

        assertThat(initialized.get(), equalTo(true));

        initialized_root.getChildren();
    }
}
