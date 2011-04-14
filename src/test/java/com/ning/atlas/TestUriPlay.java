package com.ning.atlas;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestUriPlay
{
    @Test
    public void testBasicScheme() throws Exception
    {
        URI uri = URI.create("ec2:ami-12345");
        assertThat(uri.getScheme(), equalTo("ec2"));
        assertThat(uri.getSchemeSpecificPart(), equalTo("ami-12345"));
    }

    @Test
    public void testBasicSchemeExtensions() throws Exception
    {
        URI uri = URI.create("ec2:ami-12345://waffles/hello");
        assertThat(uri.getScheme(), equalTo("ec2"));
        assertThat(uri.getSchemeSpecificPart(), equalTo("ami-12345://waffles/hello"));

        assertThat(uri.getHost(), nullValue());
    }

}
