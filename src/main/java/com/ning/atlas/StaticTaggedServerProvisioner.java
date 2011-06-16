package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.Map;

public class StaticTaggedServerProvisioner implements Provisioner
{
    private final Multimap<String, String> availables = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, String>create());

    public StaticTaggedServerProvisioner()
    {

    }

    public StaticTaggedServerProvisioner(Map<String, ? extends Collection<String>> availables)
    {
        this();
        setServers(availables);
    }

    public void setServers(Map<String, ? extends Collection<String>> servers)
    {
        for (Map.Entry<String, ? extends Collection<String>> entry : servers.entrySet()) {
            this.availables.putAll(entry.getKey(), entry.getValue());
        }
    }

    public synchronized Server provision(Base base) throws UnableToProvisionServerException
    {
        String tag = base.getAttributes().get("tag");
        String host = Iterables.getFirst(this.availables.get(tag), "!!@@##");
        if (host.equals("!!@@##")) {
            throw new UnableToProvisionServerException("No server matching tag '" + tag + "' available");
        }
        this.availables.remove(tag, host);
        return new MyServer(host, base);

    }

    private static class MyServer implements Server
    {
        private final String host;
        private final Base   base;

        public MyServer(String host, Base base)
        {
            this.host = host;

            this.base = base;
        }


        public Base getBase()
        {
            return base;
        }

        @Override
        public Server initialize(ProvisionedTemplate root)
        {

            throw new UnsupportedOperationException("Not Yet Implemented!");
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
