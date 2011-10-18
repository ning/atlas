package com.ning.atlas;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Server;
import com.ning.atlas.spi.Space;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

public class StaticTaggedServerProvisioner extends BaseComponent implements Provisioner
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

    public synchronized Server provision(Base base, Node node) throws UnableToProvisionServerException
    {
        String tag = base.getAttributes().get("tag");
        String host = Iterables.getFirst(this.availables.get(tag), "!!@@##");
        if (host.equals("!!@@##")) {
            throw new UnableToProvisionServerException(node.getId(), node.getType(), node.getName(), node.getMy(),
                                                       "No server matching tag '" + tag + "' available");
        }
        this.availables.remove(tag, host);
        return new Server(host, host);

    }

    @Override
    public Future<?> provision(NormalizedServerTemplate node, Uri<Provisioner> uri, Space space, SystemMap map)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    @Override
    public String describe(NormalizedServerTemplate server, Uri<Provisioner> uri, Space space)
    {
        return "provision a server out of a static pool, with tag <tag>";
    }
}
