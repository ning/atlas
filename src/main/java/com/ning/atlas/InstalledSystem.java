package com.ning.atlas;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class InstalledSystem extends InstalledElement
{
    private final List<InstalledElement> children;

    public InstalledSystem(Identity id, String type, String name, My my, List<InstalledElement> children)
    {
        super(id, type, name, my);
        this.children = children;
    }

    @Override
    public List<? extends InstalledElement> getChildren() {
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
