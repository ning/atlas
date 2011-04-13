package com.ning.atlas.template;

import com.google.common.base.Objects;

import java.util.Collection;

public class Root
{
    private final DeployTemplate deploymentRoot;
    private final Collection<Space> spaces;

    public Root(DeployTemplate deploymentRoot, Collection<Space> spaces) {
        this.deploymentRoot = deploymentRoot;
        this.spaces = spaces;
    }

    public DeployTemplate getDeploymentRoot()
    {
        return deploymentRoot;
    }

    public Collection<Space> getSpaces()
    {
        return spaces;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
            .add("deploymentRoot",deploymentRoot)
            .add("spaces", spaces)
            .toString();
    }
}
