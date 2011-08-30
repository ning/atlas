package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import org.apache.http.annotation.Immutable;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
        top.addBase(new Base("top-base", top, "noop", Collections.<Initialization>emptyList(), ImmutableMap.<String, String>of("ami", "ami-1234")));

        Environment child = new Environment("child", Collections.<String, Provisioner>emptyMap(), Collections.<String, Initializer>emptyMap(), top);

        child.addBase(new Base("child-base", child, "noop", Collections.<Initialization>emptyList(), ImmutableMap.<String, String>of("ami", "ami-9876")));

        top.addChild(child);

        Base top_base = top.findBase("top-base").getValue();
        assertThat(top_base.getAttributes().get("ami"), equalTo("ami-1234"));

        Base child_base = top.findBase("child-base").getValue();
        assertThat(child_base.getAttributes().get("ami"), equalTo("ami-9876"));
    }

    @Test
    public void testProperties() throws Exception
    {
        Environment top = new Environment("top");
        top.addProperties(ImmutableMap.<String, String>of("breakfast", "pancake"));
        top.addBase(new Base("top-base", top, "noop", Collections.<Initialization>emptyList(), ImmutableMap.<String, String>of("ami", "ami-1234")));

        Environment child = new Environment("child", Collections.<String, Provisioner>emptyMap(), Collections.<String, Initializer>emptyMap(), top);
        child.addProperties(ImmutableMap.<String, String>of("breakfast", "waffle"));
        child.addBase(new Base("child-base", child, "noop", Collections.<Initialization>emptyList(), ImmutableMap.<String, String>of("ami", "ami-9876")));
        top.addChild(child);

        Base top_base = top.findBase("top-base").getValue();
        assertThat(top_base.getProperties(),
                   equalTo((Map<String, String>) ImmutableMap.<String, String>of("breakfast", "pancake")));

        Base child_base = top.findBase("child-base").getValue();
        assertThat(child_base.getProperties(),
                   equalTo((Map<String, String>) ImmutableMap.<String, String>of("breakfast", "waffle")));


    }
}
