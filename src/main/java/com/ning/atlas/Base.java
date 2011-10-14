package com.ning.atlas;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Base
{
    public static final ThreadLocal<Environment> DESERIALIZATION_HACK = new ThreadLocal<Environment>();

    private final Map<String, String> attributes = Maps.newConcurrentMap();

    private final String               name;
    private final List<Uri<Installer>> initializations;
    private final Uri<Provisioner>     provisioner;

    public Base(final String name,
                final Uri<Provisioner> provisioner,
                final List<Uri<Installer>> initializations,
                final Map<String, String> attributes)
    {
        this.name = name;
        this.initializations = ImmutableList.copyOf(initializations);
        this.attributes.putAll(attributes);
        this.provisioner = provisioner;
    }

    public List<Uri<Installer>> getInitializations()
    {
        return initializations;
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
    public Uri<Provisioner> getProvisioner()
    {
        return provisioner;
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


    public static Base errorBase(String base, Environment env)
    {
        return new Base(base,
                        Uri.<Provisioner>valueOf("provisioner:UNKNOWN"),
                        Collections.<Uri<Installer>>emptyList(),
                        Collections.<String, String>emptyMap());
    }
}
