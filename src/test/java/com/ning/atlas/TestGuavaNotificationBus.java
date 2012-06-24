package com.ning.atlas;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestGuavaNotificationBus
{
    @Test
    public void testFoo() throws Exception
    {

        AsyncEventBus bus = new AsyncEventBus(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                                                .setDaemon(true)
                                                                                .setNameFormat("event-bus-%d")
                                                                                .build()));


        final CountDownLatch latch = new CountDownLatch(1);
        bus.register(new Object() {
            @SuppressWarnings("unused")
			@Subscribe
            public void on(String msg) {
                System.out.println(msg);
                latch.countDown();
            }
        });

        bus.post("hello world");

        assertThat(latch.await(1, TimeUnit.SECONDS), equalTo(true));
    }


}
