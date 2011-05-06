package com.ning.atlas;

import org.junit.Test;

import java.util.HashMap;
import java.util.Stack;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestEnvironment
{
    @Test
    public void testFoo() throws Exception
    {
        Environment top = new Environment("top");
        top.addBase(new Base("top-base", new HashMap<String, String>(){{put("ami", "ami-1234");}}));

        Environment child = new Environment("child", top.getProvisioner(), top.getInitializer());
        child.addBase(new Base("child-base", new HashMap<String, String>(){{put("ami", "ami-9876");}}));

        top.addChild(child);

        Base base = top.findBase("child-base", new Stack<String>()).otherwise(new Base("WAFFLES"));
        assertThat(base.getAttributes().get("ami"), equalTo("ami-9876"));
    }
}
