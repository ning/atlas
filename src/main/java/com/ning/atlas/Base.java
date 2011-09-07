package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Base
{
    public static final ThreadLocal<Environment> DESERIALIZATION_HACK = new ThreadLocal<Environment>();

    private final Map<String, String> attributes = Maps.newConcurrentMap();
    private final String                                  name;
    private final List<Pair<Initialization, Installer>> initializers;
    private final Pair<String, Provisioner>               provisioner;
    private final Map<String, String>                     environmentProperies;
    private final Map<String, Installer>                  installers;

//    @JsonCreator
//    public Base(@JsonProperty("name") String name, @JsonProperty("attributes") Map<String, String> attributes)
//    {
//        this(name, DESERIALIZATION_HACK.get(), attributes);
//    }


    public Base(final String name,
                final Environment e,
                final String provisionerName,
                final List<Initialization> initializationUris,
                final Map<String, String> attributes)
    {
        this.name = name;
        this.attributes.putAll(attributes);
        this.provisioner = Pair.of(provisionerName, e.getProvisioner(provisionerName));
        this.environmentProperies = e.getProperties();
        this.installers = e.getInstallers();
        this.initializers = Lists.transform(initializationUris, new Function<Initialization, Pair<Initialization, Installer>>()
        {
            @Override
            public Pair<Initialization, Installer> apply(Initialization input)
            {
                return Pair.of(input, e.getInitializers().get(input.getScheme()));
            }
        });
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public String getName()
    {
        return name;
    }

    @JsonIgnore
    public Provisioner getProvisioner()
    {
        return provisioner.getValue();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Base)) return false;

        Base base = (Base) o;

        return attributes.equals(base.attributes) && name.equals(base.name);

    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + attributes.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                      .add("name", getName())
                      .add("attributes", attributes)
                      .toString();
    }

    public Server initialize(Server server,
                             ProvisionedElement root,
                             ProvisionedServer node) throws Exception
    {
        for (Pair<Initialization, Installer> initializer : initializers) {
            initializer.getValue().install(server, initializer.getKey().getFragment(), root, node);
        }
        return server;
    }

    public List<String> getInits()
    {
        ArrayList<String> rs = Lists.newArrayListWithExpectedSize(initializers.size());
        for (Pair<Initialization, Installer> initializer : initializers) {
            rs.add(initializer.getKey().getUriForm());
        }
        return rs;
    }

    public Installer getInstaller(String prefix)
    {
        return installers.get(prefix);
    }

    @JsonIgnore
    public Map<String, String> getProperties()
    {
        return environmentProperies;
    }
}
