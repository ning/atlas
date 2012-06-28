package com.ning.atlas.spi;

import com.google.common.collect.ImmutableMap;
import com.ning.atlas.Base;
import org.junit.Test;
import org.stringtemplate.v4.ST;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestUri
{
    @Test
    public void testScheme() throws Exception
    {
        Uri<?> uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getScheme(), equalTo("provisioner"));
    }

    @Test
    public void testJustScheme() throws Exception
    {
        Uri<?> uri = Uri.valueOf("waffle");
        assertThat(uri.getScheme(), equalTo("waffle"));
        assertThat(uri.getFragment(), equalTo(""));
    }

    @Test
    public void testFragment() throws Exception
    {
        Uri<?> uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getFragment(), equalTo("galaxy:v2"));
    }

    @Test
    public void testFragmentExcludesParams() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:world?a=1&b=2");
        assertThat(uri.getFragment(), equalTo("world"));
    }

    @Test
    public void testParams1() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:world?a=1&b=2");
        Map<String, Collection<String>> params = uri.getFullParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testParams2() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello?a=1&b=2");
        Map<String, Collection<String>> params = uri.getFullParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testParams3() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:?a=1&b=2");
        Map<String, Collection<String>> params = uri.getFullParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testAdditionalParams() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:?a=1", ImmutableMap.<String, Collection<String>>of("b", Arrays.asList("2")));

        Map<String, Collection<String>> params = uri.getFullParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @Test
    public void testAdditionalParams2() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello",
                              ImmutableMap.<String, Collection<String>>of("a", Arrays.asList("1"),
                                                                          "b", Arrays.asList("2")));
        Map<String, Collection<String>> params = uri.getFullParams();
        assertThat(params.get("a"), hasItem("1"));
        assertThat(params.get("b"), hasItem("2"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testCanonicalization() throws Exception
    {
        Uri<?> uri = new Uri("hello",
                          "world",
                          ImmutableMap.<String, Collection<String>>of("a", Arrays.asList("hello", "world"),
                                                                      "b", Arrays.asList("hello world")));

        assertThat(uri.toString(), equalTo("hello:world?a=hello&a=world&b=hello world"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testSchemeWithNoStuff() throws Exception
    {
        Uri<?> uri = new Uri("rds", "", ImmutableMap.<String, Collection<String>>of("hello", Arrays.asList("world")));
        assertThat(uri.getScheme(), equalTo("rds"));
    }

    @Test
    public void testSchemeWithNoStuff2() throws Exception
    {
        Uri<?> uri = Uri.valueOf("rds", ImmutableMap.<String, Collection<String>>of("hello", Arrays.asList("world")));
        assertThat(uri.getScheme(), equalTo("rds"));
    }

    @Test
    public void testSchemeWithNoStuff3() throws Exception
    {
        Uri<?> uri = Uri.valueOf("rds", ImmutableMap.<String, Collection<String>>of("hello", Arrays.asList("world")));
        Uri<?> dup = Uri.valueOf(uri.toString());
        assertThat(dup.getScheme(), equalTo("rds"));
    }

    @Test
    public void testTemplateInUri() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:{name}");
        assertThat(uri.isTemplate(), equalTo(true));
    }

    @Test
    public void testTemplateInUri2() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:name?{key}=value");
        assertThat(uri.isTemplate(), equalTo(true));
    }

    @Test
    public void testTemplateInUri3() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:name?key={value}");
        assertThat(uri.isTemplate(), equalTo(true));
    }

    @Test
    public void testTemplateInUri4() throws Exception
    {
        Uri<?> uri = Uri.valueOf("{hello}:name?key=value");
        assertThat(uri.isTemplate(), equalTo(true));
    }

    @Test
    public void testTemplateUri5() throws Exception
    {
        Uri<?> uri = Uri.valueOf("hello:{name}?key={value}", Collections.<String, Collection<String>>emptyMap());
        assertThat(uri.toStringUnEscaped(), equalTo("hello:{name}?key={value}"));
        assertThat(uri.isTemplate(), equalTo(true));
    }

    @Test
    public void testUgh() throws Exception
    {
        Uri.valueOf("{base.fragment}?port={my.port}", Collections.<String, Collection<String>>emptyMap());
    }

    @Test
    public void testNotTemplateInUri() throws Exception
    {
        Uri<?> uri = Uri.valueOf("!hello:{name}");
        assertThat(uri.isTemplate(), equalTo(false));
        assertThat(uri.getScheme(), equalTo("hello"));
    }

    @Test
    public void testNotTemplateInUri2() throws Exception
    {
        Uri<?> uri = Uri.valueOf("!hello:name?{key}=value");
        assertThat(uri.isTemplate(), equalTo(false));
    }

    @Test
    public void testNotTemplateInUri3() throws Exception
    {
        Uri<?> uri = Uri.valueOf("!hello:name?key={value}");
        assertThat(uri.isTemplate(), equalTo(false));
    }

    @Test
    public void testNotTemplateInUri4() throws Exception
    {
        Uri<?> uri = Uri.valueOf("!{hello}:name?key=value");
        assertThat(uri.isTemplate(), equalTo(false));
        assertThat(uri.getScheme(), equalTo("{hello}"));
    }

    @Test
    public void testActuallyApplyTemplate() throws Exception
    {
        Uri<Base> base_uri = Uri.valueOf("mysql:blog");

        Uri<Provisioner> uri = Uri.valueOf("rds?name={base.fragment}&engine=MySQL");

        assertThat(uri.toStringUnEscaped(), equalTo("rds?engine=MySQL&name={base.fragment}"));

        ST st = new ST(uri.toStringUnEscaped(), '{', '}');
        st.add("base", base_uri);
        assertThat(st.render(), equalTo("rds?engine=MySQL&name=blog"));
    }

    @Test
    public void testSpaces() throws Exception
    {
        Uri<String> u = Uri.valueOf("script:sculptor/install.sh {virtual.fragment}?unwind=sculptor/uninstall.sh {virtual.fragment}");
        String unwind = u.getParams().get("unwind");
        assertThat(unwind, equalTo("sculptor/uninstall.sh {virtual.fragment}"));

    }
}
