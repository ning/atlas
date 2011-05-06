package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

public class StaticTaggedServerProvisioner implements Provisioner
{
    private final Multimap<String, String> availables = ArrayListMultimap.create();

    public StaticTaggedServerProvisioner(Map<String, ? extends Collection<String>> availables)
    {
        for (Map.Entry<String, ? extends Collection<String>> entry : availables.entrySet()) {
            this.availables.putAll(entry.getKey(), entry.getValue());
        }
    }

    public void destroy(Collection<Server> servers)
    {
        for (Server server : servers) {
            availables.put(server.getBase().getName(), server.getExternalIpAddress());
        }
    }

    public Server provision(Base base)
    {
        String host = Iterables.getFirst(this.availables.get(base.getName()), "!!@@##");
        if (host.equals("!!@@##")) {
            throw new IllegalStateException("unable to allocate a needed base='" + base + "'");
        }
        this.availables.remove(base, host);
        return new MyServer(host, base);

    }

    private static class MyServer implements Server
    {
        private final String host;
        private final Base base;


        public MyServer(String host, Base base)
        {
            this.host = host;

            this.base = base;
        }


        public Base getBase()
        {
            return base;
        }

        public String getExternalIpAddress()
        {
            return host;
        }

        public String getInternalIpAddress()
        {
            return host;
        }

        @Override
        public String toString()
        {
            return Objects.toStringHelper(this)
                .add("externalIpAddress", getExternalIpAddress())
                .add("internalIpAddress", getInternalIpAddress())
                .add("base", getBase())
                .toString();
        }
    }
}
