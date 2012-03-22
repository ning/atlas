package com.ning.atlas.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestTypesafeConfig
{
    @Test
    public void testFoo() throws Exception
    {
        Config conf = ConfigFactory.load("foo").withFallback(ConfigFactory.load("bar"));
        assertThat(conf.getInt("foo.bar"), equalTo(7));
        assertThat(conf.getInt("foo.bang"), equalTo(9));
    }
}
