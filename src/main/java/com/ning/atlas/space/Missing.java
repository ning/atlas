package com.ning.atlas.space;

public enum Missing
{


    RequireAll
        {
            @Override
            public Object missing(Class<?> t)
            {
                throw new IllegalArgumentException("all properties must be present");
            }
        },
    NullProperty
        {
            @Override
            public Object missing(Class<?> t)
            {
                return null;
            }
        };

    public abstract Object missing(Class<?> t);
}
