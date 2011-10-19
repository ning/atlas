package com.ning.atlas;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;

import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestJavaURI
{
    @Test
    public void testBasicScheme() throws Exception
    {
        URI uri = URI.create("cruft:ami-12345");
        assertThat(uri.getScheme(), equalTo("cruft"));
        assertThat(uri.getSchemeSpecificPart(), equalTo("ami-12345"));
    }

    @Test
    public void testBasicSchemeExtensions() throws Exception
    {
        URI uri = URI.create("cruft:ami-12345://waffles/hello");
        assertThat(uri.getScheme(), equalTo("cruft"));
        assertThat(uri.getSchemeSpecificPart(), equalTo("ami-12345://waffles/hello"));

        assertThat(uri.getHost(), nullValue());
    }

    @Test
    public void testParams() throws Exception
    {
        URI uri = URI.create("cruft://waffles/hello?name=Happy");
        List<NameValuePair> pairs = URLEncodedUtils.parse(uri, "UTF-8");
        assertThat(pairs.size(), equalTo(1));
        for (NameValuePair pair : pairs) {
            assertThat(pair.getName(), equalTo("name"));
            assertThat(pair.getValue(), equalTo("Happy"));
        }
    }

}
