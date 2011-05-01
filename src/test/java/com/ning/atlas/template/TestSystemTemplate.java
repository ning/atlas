package com.ning.atlas.template;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestSystemTemplate
{
    @Test
    public void testFoo() throws Exception
    {
        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("root");
        ConfigurableSystemTemplate aclu = new ConfigurableSystemTemplate("aclu");
        aclu.addChild(new ConfigurableServerTemplate("appcore"), 1);
        root.addChild(aclu, 1);

        List<String> rs = root.visit(new ArrayList<String>(), new Visitor<List<String>>()
        {

            public List<String> enterSystem(ConfigurableSystemTemplate system, int cardinality, List<String> baton)
            {
                baton.add("+" + system.getName());
                return baton;
            }

            public List<String> leaveSystem(ConfigurableSystemTemplate node, int cardinality, List<String> baton)
            {
                baton.add("-" + node.getName());
                return baton;
            }

            public List<String> visitServer(ConfigurableServerTemplate service, int cardinality, List<String> baton)
            {
                baton.add(service.getName());
                return baton;
            }
        });

        assertEquals(rs, Arrays.<String>asList("+root", "+aclu", "appcore", "-aclu", "-root"));
    }
}

