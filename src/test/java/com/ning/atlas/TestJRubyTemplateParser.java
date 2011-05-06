package com.ning.atlas;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestJRubyTemplateParser
{
//    @Test
//    public void testFoo() throws Exception
//    {
//        JRubySystemTemplateParser p = new JRubySystemTemplateParser();
//        DeployTemplate t = p.parse(new File("src/test/ruby/ex1/system-template.rb"));
//        assertThat(t, notNullValue());
//        NormalizedTemplate d = NormalizedTemplate.build(new EnvironmentConfig(new Environment("test")), t);
//
//        assertThat(d.getInstances().size(), equalTo(24));
//
//        verify aka worked for the server image
//        assertThat(d.getInstances().get(0).getBase(), equalTo("ubuntu-small"));
//    }

        @Test
        public void testwaffles() throws Exception
        {
            assertThat(1 + 1, equalTo(2));
        }

}
