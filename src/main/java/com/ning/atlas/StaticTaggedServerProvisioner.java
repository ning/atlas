package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.template.ServerSpec;
import com.ning.atlas.template.SystemAssignment;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class StaticTaggedServerProvisioner implements Provisioner
{
    private final Multimap<String, String> availables = ArrayListMultimap.create();

    public StaticTaggedServerProvisioner(Map<String, ? extends Collection<String>> availables)
    {
        for (Map.Entry<String, ? extends Collection<String>> entry : availables.entrySet()) {
            this.availables.putAll(entry.getKey(), entry.getValue());
        }
    }

    public Set<Server> provisionBareServers(SystemAssignment m) throws InterruptedException
    {
        Set<Server> rs = Sets.newLinkedHashSet();
        for (ServerSpec spec : m.getInstances()) {
            String base = spec.getBase();
            String host = Iterables.getFirst(this.availables.get(base), "!!@@##");
            if (host.equals("!!@@##")) {
                throw new IllegalStateException("unable to allocate a needed base='" + base + "'");
            }
            this.availables.remove(base, host);
            rs.add(new MyServer(host, spec));
        }
        return rs;
    }

    public void destroy(Collection<Server> servers)
    {
        for (Server server : servers) {
            availables.put(server.getBase(), server.getExternalIpAddress());
        }
    }

    private static class MyServer implements Server
    {
        private final String host;
        private final ServerSpec spec;


        public MyServer(String host, ServerSpec spec)
        {
            this.host = host;
            this.spec = spec;
        }

        public String getName()
        {
            return spec.getName();
        }

        public String getBase()
        {
            return spec.getBase();
        }

        public String getExternalIpAddress()
        {
            return host;
        }

        public String getInternalIpAddress()
        {
            return host;
        }

        public String getBootStrap()
        {
            return spec.getBootStrap();
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                .add("name", getName())
                .add("externalIpAddress", getExternalIpAddress())
                .add("internalIpAddress", getInternalIpAddress())
                .add("bootStrap", getBootStrap())
                .add("base", getBase())
                .toString();
        }
    }
}
