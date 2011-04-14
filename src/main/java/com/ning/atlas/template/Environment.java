package com.ning.atlas.template;

import com.google.common.base.Objects;

public class Environment
{
    private final String name;

    public Environment(String name) {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
            .add("name", name)
            .toString();
    }
}
