package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collections;
import java.util.List;

public class Base
{
    private final List<Uri<Installer>> initializations;
    private final Uri<Provisioner>     provisioner;

    public Base(Base inheritFrom, Uri<Provisioner> provisioner, List<Uri<Installer>> inits)
    {
        this((provisioner == null ? inheritFrom.getProvisionUri() : provisioner),
             Lists.newArrayList(Iterables.concat(inheritFrom.getInitializations(), inits)));
    }

    public Base(final Uri<Provisioner> provisioner,
                final List<Uri<Installer>> initializations)
    {
        this.initializations = ImmutableList.copyOf(initializations);
        this.provisioner = provisioner;
    }

    public List<Uri<Installer>> getInitializations()
    {
        return initializations;
    }

    @JsonIgnore
    public Uri<Provisioner> getProvisionUri()
    {
        return provisioner;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof Base && EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }


    public static Base errorBase()
    {
        return new Base(Uri.<Provisioner>valueOf("provisioner:UNKNOWN"),
                        Collections.<Uri<Installer>>emptyList());
    }
}
