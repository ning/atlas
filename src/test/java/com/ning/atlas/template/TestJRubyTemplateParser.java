package com.ning.atlas.template;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestJRubyTemplateParser
{
    @Test
    public void testFoo() throws Exception
    {
        JRubyTemplateParser p = new JRubyTemplateParser();
        SystemTemplate t = p.parse(new File("src/test/ruby/ex1/system-template.rb"));
        assertThat(t, notNullValue());
        Deployment d = Deployment.build(new EnvironmentConfig(), t);

        assertThat(d.getInstances().size(), equalTo(13));

        for (Instance instance : d.getInstances()) {
            System.out.println(instance.getTemplate().getImage());
        }

    }
}
