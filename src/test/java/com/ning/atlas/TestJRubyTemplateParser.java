package com.ning.atlas;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.ning.atlas.tree.Trees;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestJRubyTemplateParser
{
    @Test
    public void testFoo() throws Exception
    {
        JRubySystemTemplateParser p = new JRubySystemTemplateParser();
        Template t = p.parse(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(t, notNullValue());
        List<Template> leaves = Trees.leaves(t);
        assertThat(leaves.size(), equalTo(3));

        Template rslv_t = Iterables.find(leaves, new Predicate<Template>()
        {
            public boolean apply(@Nullable Template input)
            {
                return "resolver".equals(input.getName());
            }
        });

        assertThat(rslv_t, instanceOf(ServerTemplate.class));
        ServerTemplate rslv = (ServerTemplate) rslv_t;

        assertThat(rslv.getCardinality(), equalTo(8));
        assertThat(rslv.getBase(), equalTo(new Base("ubuntu-small")));
        assertThat(rslv.getInstallations(), hasItem("cast:load-balancer-9.3"));
    }

}
