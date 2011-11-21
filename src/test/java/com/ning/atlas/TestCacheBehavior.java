package com.ning.atlas;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestCacheBehavior
{
    @Test
    public void testFoo() throws Exception
    {
        final Set<String> whacked = new ConcurrentSkipListSet<String>();
        Cache<String, Integer> sizes = CacheBuilder
            .newBuilder()
            .removalListener(new RemovalListener<String, Integer>()
            {
                @Override
                public void onRemoval(RemovalNotification<String, Integer> event)
                {
                    whacked.add(event.getKey());
                }
            })
            .maximumSize(Integer.MAX_VALUE)
            .build(new CacheLoader<String, Integer>()
            {
                @Override
                public Integer load(String key) throws Exception
                {
                    return key.length();
                }
            });

        assertThat(sizes.get("hello"), equalTo(5));

        sizes.invalidateAll();
        assertThat(whacked.size(), equalTo(1));
        assertThat(whacked.contains("hello"), equalTo(true));
    }
}
