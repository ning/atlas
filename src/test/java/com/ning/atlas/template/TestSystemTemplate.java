package com.ning.atlas.template;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestSystemTemplate
{
    @Test
    public void testFoo() throws Exception
    {
        SystemTemplate root = new SystemTemplate("root");
        SystemTemplate aclu = new SystemTemplate("aclu");
        aclu.addChild(new ServerTemplate("appcore", Collections.<String>emptyList()), 1);
        root.addChild(aclu, 1);

        List<String> rs = root.visit(new ArrayList<String>(), new Visitor<List<String>>()
        {

            public List<String> enterSystem(SystemTemplate system, int cardinality, List<String> baton)
            {
                baton.add("+" + system.getName());
                return baton;
            }

            public List<String> leaveSystem(SystemTemplate node, int cardinality, List<String> baton)
            {
                baton.add("-" + node.getName());
                return baton;
            }

            public List<String> visitService(ServerTemplate service, int cardinality, List<String> baton)
            {
                baton.add(service.getName());
                return baton;
            }
        });

        assertEquals(rs, Arrays.<String>asList("+root", "+aclu", "appcore", "-aclu", "-root"));
    }
}

