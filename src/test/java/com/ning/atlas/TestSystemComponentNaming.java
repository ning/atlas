package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.template.EnvironmentConfig;
import com.ning.atlas.template.ServerTemplate;
import com.ning.atlas.template.SystemAssignment;
import com.ning.atlas.template.SystemTemplate;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TestSystemComponentNaming
{
    @Test
    public void testExploratory() throws Exception
    {
        SystemTemplate root = new SystemTemplate("animal");
        SystemTemplate dog = new SystemTemplate("dog");
        dog.addChild(new ServerTemplate("Bean", "waffles", ""), 1);
        root.addChild(dog, 1);
        SystemTemplate cat = new SystemTemplate("cat");
        cat.addChild(new ServerTemplate("Moose", "waffles", ""), 1);
        root.addChild(cat, 1);

        SystemAssignment sa =  SystemAssignment.build(new EnvironmentConfig(), root);
        Map<String, Collection<String>> servers = Maps.newHashMap();
        servers.put("waffles", Lists.newArrayList("10.0.0.1", "10.0.0.2"));

        StaticTaggedServerProvisioner prov = new StaticTaggedServerProvisioner(servers);
        Set<Server> bs = prov.provisionBareServers(sa);
        for (Server b : bs) {
            System.out.println(b);
        }
    }
}

