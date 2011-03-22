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
        Manifest d = Manifest.build(new EnvironmentConfig(), t);

        assertThat(d.getInstances().size(), equalTo(23));

        // verify aka worked for the server image

        assertThat("ami-a6f504cf", equalTo(d.getInstances().get(0).getImage()));
    }
}
