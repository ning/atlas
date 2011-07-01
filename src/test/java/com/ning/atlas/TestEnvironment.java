package com.ning.atlas;

import org.junit.Test;

import java.util.HashMap;
import java.util.Stack;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestEnvironment
{
    @Test
    public void testFoo() throws Exception
    {
        Environment top = new Environment("top");
        top.addBase(new Base("top-base", top, new HashMap<String, String>()
        {{put("ami", "ami-1234");}}));

        Environment child = new Environment("child", top.getProvisioner(), top.getInitializers(), top);
        child.addBase(new Base("child-base", child, new HashMap<String, String>()
        {{put("ami", "ami-9876");}}));

        top.addChild(child);

        Base top_base = top.findBase("top-base", new Stack<String>()).getValue();
        assertThat(top_base.getAttributes().get("ami"), equalTo("ami-1234"));

        Base child_base = top.findBase("child-base", new Stack<String>()).getValue();
        assertThat(child_base.getAttributes().get("ami"), equalTo("ami-9876"));
    }

    @Test
    public void testNestedEnvironmentsWithDifferentProvisioners() throws Exception
    {
        Provisioner top_provisioner = new ErrorProvisioner();
        Environment top = new Environment("top");
        top.setProvisioner(top_provisioner);
        top.addBase(new Base("top-base", top, new HashMap<String, String>()
        {{put("ami", "ami-1234");}}));

        Provisioner child_provisioner = new ErrorProvisioner();
        Environment child = new Environment("child");
        child.setProvisioner(child_provisioner);
        child.addBase(new Base("child-base", child, new HashMap<String, String>()
        {{put("ami", "ami-9876");}}));
        top.addChild(child);

        Base top_base = top.findBase("top-base", new Stack<String>()).getValue();
        assertThat(top_base.getAttributes().get("ami"), equalTo("ami-1234"));

        Base child_base = top.findBase("child-base", new Stack<String>()).getValue();
        assertThat(child_base.getAttributes().get("ami"), equalTo("ami-9876"));


        assertThat(child_base.getProvisioner(), is(child_provisioner));
        assertThat(top_base.getProvisioner(), is(top_provisioner));
    }
}
