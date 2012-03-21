package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFutureTask;
import com.kenai.constantine.platform.Errno;
import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIXFactory;
import org.jruby.ext.posix.POSIXHandler;
import org.jruby.ext.posix.util.Finder;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestLibraryBehaviors
{
    @Test
    public void testFoo() throws Exception
    {
        POSIX posix = POSIXFactory.getPOSIX(new POSIXHandler() {
            @Override
            public void error(Errno errno, String s)
            {
            }

            @Override
            public void unimplementedError(String s)
            {
            }

            @Override
            public void warn(WARNING_ID warning_id, String s, Object... objects)
            {
            }

            @Override
            public boolean isVerbose()
            {
                return false;
            }

            @Override
            public File getCurrentWorkingDirectory()
            {
                return null;
            }

            @Override
            public String[] getEnv()
            {
                return new String[0];
            }

            @Override
            public InputStream getInputStream()
            {
                return null;
            }

            @Override
            public PrintStream getOutputStream()
            {
                return null;
            }

            @Override
            public int getPID()
            {
                return 0;
            }

            @Override
            public PrintStream getErrorStream()
            {
                return null;
            }
        }, true);
        String ssh =  Finder.findFileInPath(posix, "ssh", System.getenv("PATH"));
        System.out.println(ssh);
    }

    @Test
    public void testListenableFuture() throws Exception
    {
        final Executor e = Executors.newFixedThreadPool(2);

        final CountDownLatch latch = new CountDownLatch(1);
        ListenableFutureTask<String> t = ListenableFutureTask.create(new Callable<String>() {
            public String call() throws Exception
            {
                latch.countDown();
                return "hello world";
            }
        });


        final CountDownLatch listener_latch = new CountDownLatch(1);
        t.addListener(new Runnable() {
            public void run()
            {
                listener_latch.countDown();
            }
        }, e);


        e.execute(t);

        assertThat(latch.await(1, TimeUnit.SECONDS), equalTo(true));
        assertThat(listener_latch.await(1, TimeUnit.SECONDS), equalTo(true));
    }
}
