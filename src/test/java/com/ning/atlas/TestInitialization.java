package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.util.Arrays;
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
        List<? extends ProvisionedTemplate> children = asList(new ProvisionedServerTemplate("server", new Server()
        {
            @Override
            public String getExternalIpAddress()
            {
                return "10.0.0.1";
            }

            @Override
            public String getInternalIpAddress()
            {
                return "10.0.0.1";
            }

            @Override
            public ListenableFuture<? extends Server> initialize()
            {
                initialized.set(true);
                return Futures.immediateFuture(this);
            }
        }));

        ProvisionedSystemTemplate root = new ProvisionedSystemTemplate("root", children);
        InitializedTemplate initialized_root = root.initialize().get();
        assertThat(initialized.get(), equalTo(true));

        initialized_root.getChildren();

    }
}