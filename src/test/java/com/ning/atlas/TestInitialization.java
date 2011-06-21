package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.chef.StubServer;
import org.junit.Test;

import javax.rmi.CORBA.Stub;
import java.util.Arrays;
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
        List<? extends ProvisionedTemplate> children = asList(
            new ProvisionedServerTemplate("server", "0", new My(), new StubServer("10.0.0.1")
            {
                @Override
                public Server initialize(ProvisionedTemplate root)
                {
                    initialized.set(true);
                    return this;
                }
            }, Collections.<String>emptyList()));

        ProvisionedSystemTemplate root = new ProvisionedSystemTemplate("root", "0", new My(), children);
        InitializedTemplate initialized_root = root.initialize(MoreExecutors.sameThreadExecutor(), root).get();
        assertThat(initialized.get(), equalTo(true));

        initialized_root.getChildren();
    }
}
