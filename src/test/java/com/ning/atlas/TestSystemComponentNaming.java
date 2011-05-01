package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.template.ConfigurableServerTemplate;
import com.ning.atlas.template.ConfigurableSystemTemplate;
import com.ning.atlas.template.Environment;
import com.ning.atlas.template.EnvironmentConfig;
import com.ning.atlas.template.NormalizedTemplate;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TestSystemComponentNaming
{
    @Test
    public void testExploratory() throws Exception
    {
        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("animal");
        ConfigurableSystemTemplate dog = new ConfigurableSystemTemplate("dog");
        dog.addChild(new ConfigurableServerTemplate("Bean", "waffles", ""), 1);
        root.addChild(dog, 1);
        ConfigurableSystemTemplate cat = new ConfigurableSystemTemplate("cat");
        cat.addChild(new ConfigurableServerTemplate("Moose", "waffles", ""), 1);
        root.addChild(cat, 1);

        NormalizedTemplate sa =  NormalizedTemplate.build(new EnvironmentConfig(new Environment("waffle")), root);
        Map<String, Collection<String>> servers = Maps.newHashMap();
        servers.put("waffles", Lists.newArrayList("10.0.0.1", "10.0.0.2"));

        StaticTaggedServerProvisioner prov = new StaticTaggedServerProvisioner(servers);
        Set<Server> bs = prov.provisionBareServers(sa);
        for (Server b : bs) {
            System.out.println(b);
        }
    }
}

