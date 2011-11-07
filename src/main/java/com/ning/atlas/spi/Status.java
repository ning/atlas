package com.ning.atlas.spi;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class Status
{
    private final String msg;
    private final Type type;

    private Status(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getMessage()
    {
        return msg;
    }

    public Type getType()
    {
        return type;
    }

    /**
     * Indicates that the current deployment should abort. This should be done for
     * catastrophic things.
     */
    public static Status abort(String msg) {
        return new Status(Type.Abort, msg);
    }

    /**
     * Used to indicate that the activity failed, should include a descriptive message
     * suitable for explaining what failed, but not diagnostic information. Log that.
     */
    public static Status fail(String msg)
    {
        return new Status(Type.Failure, msg);
    }

    /**
     * Used to indicate the operation proceeded along the happy path.
     */
    public static Status okay() {
        return okay("Okay");
    }

    public static Status okay(String msg) {
        return new Status(Type.Okay, "Okay");
    }

    public static enum Type
    {
        Okay, Failure, Abort
    }
}
