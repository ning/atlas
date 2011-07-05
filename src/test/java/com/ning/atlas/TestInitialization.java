package com.ning.atlas;

import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.chef.StubServer;
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
            public Server initialize(Server server, String arg, ProvisionedTemplate root,
                      ProvisionedServerTemplate node)
            {
                initialized.set(true);
                assertThat(arg, equalTo("meow"));
                return server;
            }
        };

        Environment env = new Environment("env");
        env.addInitializer("woof", initializer);

        Base base = new Base("server", env);
        base.addInit("woof:meow");

        List<? extends ProvisionedTemplate> children = asList(
            new ProvisionedServerTemplate("server", "0", new My(),
                                          new StubServer("10.0.0.1", base), Collections.<String>emptyList()));

        ProvisionedSystemTemplate root = new ProvisionedSystemTemplate("root", "0", new My(), children);
        InitializedTemplate initialized_root = root.initialize(MoreExecutors.sameThreadExecutor()).get();

        assertThat(initialized.get(), equalTo(true));

        initialized_root.getChildren();
    }
}
