package com.ning.atlas.spi.space;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.ning.atlas.spi.Identity;
import org.apache.commons.lang3.builder.EqualsBuilder;

public final class SpaceKey
{
    private final Identity id;
    private final String key;

    public static SpaceKey from(Identity id, String key) {
        return new SpaceKey(id, key);
    }

    private SpaceKey(Identity id, String key) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(key.length() > 0);

        this.id = id;
        this.key = key;
    }

    public final Identity getIdentity()
    {
        return id;
    }

    public final String getKey()
    {
        return key;
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id, key);
    }
}
