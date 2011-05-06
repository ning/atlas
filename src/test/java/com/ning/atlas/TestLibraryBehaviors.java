package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFutureTask;
import org.junit.Test;

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
    public void testListenableFuture() throws Exception
    {
        final Executor e = Executors.newFixedThreadPool(2);

        final CountDownLatch latch = new CountDownLatch(1);
        ListenableFutureTask<String> t = new ListenableFutureTask<String>(new Callable<String>() {
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
