package com.ning.atlas;

import com.google.common.collect.Iterables;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.tree.Trees;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Stack;

import static com.ning.atlas.base.MorePredicates.beanPropertyEquals;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJRubyTemplateParser
{
    @Test
    public void testSimpleSystem() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Template t = p.parseSystem(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(t, notNullValue());
        List<Template> leaves = Trees.leaves(t);
        assertThat(leaves.size(), equalTo(3));

        Template rslv_t = Iterables.find(leaves, beanPropertyEquals("type", "resolver"));

        assertThat(rslv_t, instanceOf(ServerTemplate.class));
        ServerTemplate rslv = (ServerTemplate) rslv_t;

        assertThat(rslv.getCardinality(), equalTo(asList("0", "1", "2", "3", "4", "5", "6", "7")));
        assertThat(rslv.getBase(), equalTo("ubuntu-small"));
        assertThat(rslv.getInstallations(), hasItem("cast:load-balancer-9.3"));
    }

    @Test
    public void testSimpleEnvironment() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/ex1/simple-environment.rb"));

        Maybe<Base> cs = e.findBase("concrete", new Stack<String>());
        assertThat(cs.getValue(), notNullValue());
        Base b = cs.getValue();
        assertThat(b.getInits(), equalTo(asList("chef-solo:{ \"run_list\": \"role[java-core]\" }")));
    }

}
