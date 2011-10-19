package com.ning.atlas.spi;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.spi.Uri;
import org.junit.Test;


import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestUri
{
    @Test
    public void testScheme() throws Exception
    {
        Uri uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getScheme(), equalTo("provisioner"));
    }

    @Test
    public void testJustScheme() throws Exception
    {
        Uri uri = Uri.valueOf("waffle");
        assertThat(uri.getScheme(), equalTo("waffle"));
        assertThat(uri.getFragment(), equalTo(""));
    }

    @Test
    public void testFragment() throws Exception
    {
        Uri uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getFragment(), equalTo("galaxy:v2"));
    }

    @Test
    public void testFragmentExcludesParams() throws Exception
    {
        Uri uri = Uri.valueOf("hello:world?a=1&b=2");
        assertThat(uri.getFragment(), equalTo("world"));
    }

    @Test
    public void testParams1() throws Exception
    {
        Uri uri = Uri.valueOf("hello:world?a=1&b=2");
        Map<String, Collection<String>> params = uri.getParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testParams2() throws Exception
    {
        Uri uri = Uri.valueOf("hello?a=1&b=2");
        Map<String, Collection<String>> params = uri.getParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testParams3() throws Exception
    {
        Uri uri = Uri.valueOf("hello:?a=1&b=2");
        Map<String, Collection<String>> params = uri.getParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testAdditionalParams() throws Exception
    {
        Uri uri = Uri.valueOf("hello:?a=1",
                              ImmutableMap.<String, Collection<String>>of("b", Arrays.asList("2")));
        Map<String, Collection<String>> params = uri.getParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testAdditionalParams2() throws Exception
    {
        Uri uri = Uri.valueOf("hello",
                              ImmutableMap.<String, Collection<String>>of("a", Arrays.asList("1"),
                                                                          "b", Arrays.asList("2")));
        Map<String, Collection<String>> params = uri.getParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testCanonicalization() throws Exception
    {
        Uri uri = new Uri("hello",
                          "world",
                          ImmutableMap.<String, Collection<String>>of("a", Arrays.asList("hello", "world"),
                                                                      "b", Arrays.asList("hello world")));

        assertThat(uri.toString(), equalTo("hello:world?a=hello&a=world&b=hello+world"));
    }
}
