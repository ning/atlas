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
        DeployTemplate t = p.parse(new File("src/test/ruby/ex1/system-template.rb")).getDeploymentRoot();
        assertThat(t, notNullValue());
        SystemManifest d = SystemManifest.build(new EnvironmentConfig(), t);

        assertThat(d.getInstances().size(), equalTo(24));

        // verify aka worked for the server image
        assertThat(d.getInstances().get(0).getBase(), equalTo("ubuntu-small"));
    }
}
