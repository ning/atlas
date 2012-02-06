package com.ning.atlas.components.aws;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestAWSConfigurator
{
    @Test
    public void testFoo() throws Exception
    {
        try {
            AWSConfigurator c = new AWSConfigurator(ImmutableMap.<String, String>of("ssh_foo", "hello@world"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSplitter() throws Exception
    {
        List<String> i = Lists.newArrayList(Splitter.onPattern("\\s+").split("hello world"));
        assertThat(i, equalTo(asList("hello", "world")));
    }
}
