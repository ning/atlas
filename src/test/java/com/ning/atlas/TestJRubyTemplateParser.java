package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ning.atlas.components.noop.NoOpInstaller;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.base.MorePredicates;
import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.tree.Trees;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import static com.ning.atlas.base.MorePredicates.beanPropertyEquals;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJRubyTemplateParser
{
    public static final JsonFactory factory = new JsonFactory(new ObjectMapper());


    @Test
    public void testDescriptorParsingEnvironment() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor d = p.parseDescriptor(new File("src/test/ruby/ex1/simple-environment.rb"));
        assertThat(d.getEnvironments().size(), equalTo(1));
    }

    @Test
    public void testDescriptorParsingTemplate() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor d = p.parseDescriptor(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(d.getTemplates().size(), equalTo(1));
    }

    @Test
    public void testDescriptorMerging() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Descriptor env = p.parseDescriptor(new File("src/test/ruby/ex1/simple-environment.rb"));
        Descriptor sys = p.parseDescriptor(new File("src/test/ruby/ex1/system-template.rb"));

        Descriptor combined = env.combine(sys);
        assertThat(combined.getEnvironments().size(), equalTo(2));
        assertThat(combined.getTemplates().size(), equalTo(2));
    }

    @Test
    public void testNormalizeDescriptor() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();

        Descriptor env = p.parseDescriptor(new File("src/test/ruby/test_jruby_template_parser_test_virtual_installer-env.rb"));
        Descriptor sys = p.parseDescriptor(new File("src/test/ruby/test_jruby_template_parser_test_virtual_installer-sys.rb"));

        Descriptor combined = env.combine(sys);
        SystemMap foo = combined.normalize("test");
        Environment test = combined.getEnvironment("test");
        ActualDeployment d = test.planDeploymentFor(foo, InMemorySpace.newInstance());
        System.out.println(d);
        d.converge();

    }

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
        assertThat(rslv.getBaseUri().getScheme(), equalTo("ubuntu-small"));
        assertThat(rslv.getInstallUris(), hasItem(Uri.<Installer>valueOf("cast:load-balancer-9.3")));
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
        Uri<Installer> u = Uri.valueOf("ubuntu-chef-solo:{ \"run_list\": \"role[java-core]\" }");
        assertThat(b.getInitUris(), equalTo(asList(u)));
    }

    @Test
    public void testParameterizedInstallersPopulated() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));

        ServerTemplate st = Iterables.find(Trees.findInstancesOf(t, ServerTemplate.class),
                                           MorePredicates.<ServerTemplate>beanPropertyEquals("type",
                                                                                             "single-param-install"));
        List<Uri<Installer>> xs = st.getInstallUris();
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
        List<Uri<Installer>> xs = st.getInstallUris();
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
        List<Uri<Installer>> xs = st.getInstallUris();
        assertThat(xs, equalTo(asList(Uri.<Installer>valueOf("hello:world"))));
    }

    @Test
    public void testEnvironmentWithListener() throws Exception
    {
        ListenerThing.calls.clear();
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment env = p.parseEnvironment(new File("src/test/ruby/ex1/env-with-listener.rb"));
        env.getPluginSystem().registerListener("testy", ListenerThing.class, Collections.<String, String>emptyMap());

        Host h = new Host(Identity.root().createChild("some", "thing"),
                          Uri.<Provisioner>valueOf("noop"),
                          Collections.<Uri<Installer>>emptyList(),
                          Collections.<Uri<Installer>>emptyList(),
                          new My());
        SystemMap map = new SystemMap(h);
        Space space = InMemorySpace.newInstance();
        ActualDeployment d = new ActualDeployment(map, env, space);

        d.converge();
        assertThat(ListenerThing.calls, equalTo(asList("startDeployment",
                                                       "startProvision",
                                                       "finishProvision",
                                                       "startInit",
                                                       "finishInit",
                                                       "startInstall",
                                                       "finishInstall",
                                                       "startUnwind",
                                                       "finishUnwind",
                                                       "finishDeployment")));
        ListenerThing.calls.clear();
    }

    @Test
    public void testExternalSystem() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template-with-external.rb"));
        Environment env = p.parseEnvironment(new File("src/test/ruby/ex1/env-with-listener.rb"));

        SystemMap map = t.normalize(env);

        SortedSet<Host> hosts = Sets.newTreeSet(new Comparator<Host>()
        {
            @Override
            public int compare(Host host, Host host1)
            {
                return host.getId().toExternalForm().compareTo(host1.getId().toExternalForm());
            }
        });

        hosts.addAll(map.findLeaves());

        assertThat(hosts.size(), equalTo(3));
        Iterator<Host> itty = hosts.iterator();
        Host one = itty.next();
        System.out.println(one.getId());
        Host two = itty.next();
        System.out.println(two.getId());
        Host three = itty.next();
        System.out.println(three.getId());
    }

    @Test
    public void testTemplatization() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_jruby_template_parser_templatization-env.rb"));
        Template t = p.parseSystem(new File("src/test/ruby/test_jruby_template_parser_templatization-sys.rb"));

        SystemMap map = t.normalize(e);
        assertThat(map.findLeaves().size(), equalTo(1));
        Host h = Iterables.getOnlyElement(map.findLeaves());
        assertThat(h.getProvisionerUri().getParams().get("name"), equalTo("blog"));

    }

    @Test
    public void testEnvironmentDefinedElements() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_jruby_template_parser_env_servers-env.rb"));
        Template t = p.parseSystem(new File("src/test/ruby/test_jruby_template_parser_env_servers-sys.rb"));

        SystemMap map = t.normalize(e);
        assertThat(map.findLeaves().size(), equalTo(2));
    }

    @Test
    public void testVirtualInstaller() throws Exception
    {
        NoOpInstaller.reset();
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_jruby_template_parser_test_virtual_installer-env.rb"));
        Template t = p.parseSystem(new File("src/test/ruby/test_jruby_template_parser_test_virtual_installer-sys.rb"));

        SystemMap map = t.normalize(e);
        assertThat(map.findLeaves().size(), equalTo(1));

        Host h = Iterables.getOnlyElement(map.findLeaves());
        assertThat(ImmutableList.copyOf(h.getInstallationUris()),
                   equalTo(ImmutableList.of(Uri.<Installer>valueOf("noop:3.0.2"),
                                            Uri.<Installer>valueOf("noop:octopus"))));
    }

}
