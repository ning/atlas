package com.ning.atlas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestSnakeYamlBehavior
{
    @Test
    public void testFoo() throws Exception
    {
        Map<String, Object> it = (Map<String, Object>) new Yaml().load("top:\n  child1: hello\n  child2: 7");
        Map<String, Object> rs = Maps.newHashMap();
        rs.put("child1", "hello");
        rs.put("child2", 7);
        assertThat(it.get("top"), equalTo((Object) rs));
    }
}
