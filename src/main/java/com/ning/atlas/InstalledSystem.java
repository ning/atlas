package com.ning.atlas;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class InstalledSystem extends InstalledTemplate
{
    private final List<InstalledTemplate> children;

    public InstalledSystem(String type, String name, My my, List<InstalledTemplate> children)
    {
        super(type, name, my);
        this.children = children;
    }

    @Override
    public List<InstalledTemplate> getChildren() {
        return this.children;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}
