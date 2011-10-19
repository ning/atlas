package com.ning.atlas;

import com.google.common.collect.Iterables;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.base.MorePredicates;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.tree.Trees;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.ning.atlas.base.MorePredicates.beanPropertyEquals;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJRubyTemplateParser
{
    public static final JsonFactory factory = new JsonFactory(new ObjectMapper());

    @Test
    public void testSimpleSystem() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(t, notNullValue());
        List<Template> leaves = Trees.leaves(t);
        assertThat(leaves.size(), equalTo(7));

        Template rslv_t = Iterables.find(leaves, beanPropertyEquals("type", "resolver"));

        assertThat(rslv_t, instanceOf(ServerTemplate.class));
        ServerTemplate rslv = (ServerTemplate) rslv_t;

        assertThat(rslv.getCardinality(), equalTo(asList("0", "1", "2", "3", "4", "5", "6", "7")));
        assertThat(rslv.getBase(), equalTo("ubuntu-small"));
        assertThat(rslv.getInstallations(), hasItem(Uri.<Installer>valueOf("cast:load-balancer-9.3")));
    }

    @Test
    public void testCardinalityAsArray() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(t, notNullValue());

        List<Template> rs = Trees.find(t, MorePredicates.<Template>beanPropertyEquals("type", "aclu"));
        assertThat(rs.size(), equalTo(1));
        assertThat(rs.get(0), instanceOf(SystemTemplate.class));
        SystemTemplate aclu = (SystemTemplate) rs.get(0);
        assertThat(aclu.getCardinality(), equalTo(asList("aclu0", "aclu1")));
    }

    @Test
    public void testMyAttributesPopulated() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));

        List<Template> leaves = Trees.leaves(t);

        Template appc = Iterables.find(leaves, beanPropertyEquals("type", "appcore"));
        My my = appc.getMy();
        JsonNode json = factory.createJsonParser(my.toJson()).readValueAsTree();
        assertThat(json.get("waffle").getIntValue(), equalTo(7));

        JsonNode r = json.get("xn.raspberry");
        assertThat(r.get(0).getIntValue(), equalTo(1));
        assertThat(r.get(1).getIntValue(), equalTo(2));
        assertThat(r.get(2).getIntValue(), equalTo(3));
    }

    @Test
    public void testSimpleEnvironment() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/ex1/simple-environment.rb"));

        Maybe<Base> cs = e.findBase("concrete");
        assertThat(cs.getValue(), notNullValue());
        Base b = cs.getValue();
        Uri<Installer> u = Uri.valueOf("chef-solo:{ \"run_list\": \"role[java-core]\" }");
        assertThat(b.getInitializations(), equalTo(asList(u)));
    }

    @Test
    public void testParameterizedInstallersPopulated() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));

        ServerTemplate st = Iterables.find(Trees.findInstancesOf(t, ServerTemplate.class),
                                           MorePredicates.<ServerTemplate>beanPropertyEquals("type",
                                                                                             "single-param-install"));
        List<Uri<Installer>> xs = st.getInstallations();
        assertThat(xs, equalTo(asList(Uri.<Installer>valueOf("foo:bar?size=7"))));

    }

    @Test
    public void testInstallers2() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));

        ServerTemplate st = Iterables.find(Trees.findInstancesOf(t, ServerTemplate.class),
                                           MorePredicates.<ServerTemplate>beanPropertyEquals("type",
                                                                                             "single-param-install2"));
        List<Uri<Installer>> xs = st.getInstallations();
        assertThat(xs, equalTo(asList(Uri.<Installer>valueOf("foo:bar?size=7"))));

    }

    @Test
    public void testInstallers3() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));

        ServerTemplate st = Iterables.find(Trees.findInstancesOf(t, ServerTemplate.class),
                                           MorePredicates.<ServerTemplate>beanPropertyEquals("type",
                                                                                             "single-param-install4"));
        List<Uri<Installer>> xs = st.getInstallations();
        assertThat(xs, equalTo(asList(Uri.<Installer>valueOf("hello:world"))));
    }
}
