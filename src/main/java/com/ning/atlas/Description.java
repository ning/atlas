package com.ning.atlas;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Description
{
    private final List<HostDeploymentDescription> descriptors;

    public Description(Iterable<HostDeploymentDescription> values)
    {
        this.descriptors = ImmutableList.copyOf(values);
    }

    public List<HostDeploymentDescription> getDescriptors()
    {
        return descriptors;
    }
}
