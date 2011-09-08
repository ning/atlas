package com.ning.atlas;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestIdentity
{
    @Test
    public void testToExternalForm() throws Exception
    {
        Identity root = Identity.root();
        Identity child_1 = root.createChild("hello", "0");
        Identity child_2 = child_1.createChild("world", "0");

        assertThat(root.toExternalForm(), equalTo("/"));
        assertThat(child_1.toExternalForm(), equalTo("/hello.0"));
        assertThat(child_2.toExternalForm(), equalTo("/hello.0/world.0"));
    }

    @Test
    public void testFromExternalForm() throws Exception
    {
        Identity root = Identity.root();
        Identity child_1 = root.createChild("hello", "0");
        Identity child_2 = child_1.createChild("world", "0");

        assertThat(Identity.valueOf(child_2.toExternalForm()), equalTo(child_2));
        assertThat(Identity.valueOf(child_1.toExternalForm()), equalTo(child_1));
        assertThat(Identity.valueOf(root.toExternalForm()), equalTo(root));

    }


}
