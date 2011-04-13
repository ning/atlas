package com.ning.atlas.template;

import com.google.common.base.Objects;

public class Space
{
    private final String name;

    public Space(String name) {
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
