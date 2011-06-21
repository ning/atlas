package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.chef.StubServer;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestInstallation
{
    @Test
    public void testFoo() throws Exception
    {
        final AtomicBoolean installed = new AtomicBoolean(false);
        InitializedServerTemplate child = new InitializedServerTemplate("server", "0", new My(), new StubServer("10.0.0.1") {
            @Override
            public Server install()
            {
                installed.set(true);
                return super.install();
            }
        });
        InitializedSystemTemplate root = new InitializedSystemTemplate("top", "0", new My(), asList(child));

        ListenableFuture<? extends InstalledTemplate> f = root.install(MoreExecutors.sameThreadExecutor());
        InstalledTemplate it = f.get();
        assertThat(it, notNullValue());
        assertThat(installed.get(), equalTo(true));
    }
}
