package com.ning.atlas.spi;

import com.google.common.base.Function;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * From https://github.com/npryce/
 */
public abstract class Maybe<T> implements Iterable<T>
{
    public abstract T otherwise(T defaultValue);

    public abstract Maybe<T> otherwise(Maybe<T> maybeDefaultValue);

    public abstract T elsewise(Callable<T> c) throws Exception;

    public abstract <U> Maybe<U> to(Function<? super T, ? extends U> mapping);

    public abstract T getValue();

    public abstract boolean isKnown();

    public abstract <E extends Exception> T otherwise(E e) throws E;

    public static <T> Maybe<T> definitely(final T theValue)
    {
        return new DefiniteValue<T>(theValue);
    }


    public static <T> Maybe<T> elideNull(T value)
    {
        return value == null ? Maybe.<T>unknown() : definitely(value);
    }


    public static <T> Maybe<T> unknown()
    {
        return new Maybe<T>()
        {
            @Override
            public boolean isKnown()
            {
                return false;
            }

            public Iterator<T> iterator()
            {
                return Collections.<T>emptyList().iterator();
            }

            @Override
            public T otherwise(T defaultValue)
            {
                return defaultValue;
            }

            @Override
            public Maybe<T> otherwise(Maybe<T> maybeDefaultValue)
            {
                return maybeDefaultValue;
            }

            @Override
            public T elsewise(Callable<T> c) throws Exception
            {
                return c.call();
            }

            @Override
            public <U> Maybe<U> to(Function<? super T, ? extends U> mapping)
            {
                return unknown();
            }

            @Override
            public T getValue()
            {
                throw new IllegalStateException("No value known!");
            }

            @Override
            public <E extends Exception> T otherwise(E e) throws E
            {
                throw e;
            }

            @Override
            public String toString()
            {
                return "unknown";
            }

            @Override
            public boolean equals(Object obj)
            {
                return false;
            }

            @Override
            public int hashCode()
            {
                return 0;
            }
        };
    }

    private static class DefiniteValue<T> extends Maybe<T>
    {
        private final T theValue;

        public DefiniteValue(T theValue)
        {
            this.theValue = theValue;
        }

        @Override
        public boolean isKnown()
        {
            return true;
        }

        public Iterator<T> iterator()
        {
            return Collections.singleton(theValue).iterator();
        }

        @Override
        public T otherwise(T defaultValue)
        {
            return theValue;
        }

        @Override
        public Maybe<T> otherwise(Maybe<T> maybeDefaultValue)
        {
            return this;
        }

        @Override
        public T elsewise(Callable<T> c) throws Exception
        {
            return theValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Maybe<U> to(Function<? super T, ? extends U> mapping)
        {
            // cast is to make e.g. Eclipse happy with this line
            return (Maybe<U>) definitely(mapping.apply(theValue));
        }

        @Override
        public T getValue()
        {
            return theValue;
        }

        @Override
        public <E extends Exception> T otherwise(E e) throws E
        {
            return theValue;
        }

        @Override
        public String toString()
        {
            return "definitely " + theValue.toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefiniteValue<?> that = DefiniteValue.class.cast(o);
            return theValue.equals(that.theValue);
        }

        @Override
        public int hashCode()
        {
            return theValue.hashCode();
        }
    }
}