package com.ning.atlas.base;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MorePredicates
{
    public static <T> Predicate<T> beanPropertyEquals(final String name, final Object value)
    {
        return new Predicate()
        {
            public boolean apply(@Nullable Object input)
            {
                try {
                    Method m = input.getClass().getMethod("get"
                                                          + name.substring(0, 1).toUpperCase()
                                                          + name.substring(1, name.length()));
                    return m.invoke(input).equals(value);
                }
                catch (NoSuchMethodException e) {
                    return false;
                }
                catch (InvocationTargetException e) {
                    throw new IllegalArgumentException(e);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }
}
