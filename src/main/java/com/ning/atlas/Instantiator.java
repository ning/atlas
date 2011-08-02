package com.ning.atlas;

import java.lang.reflect.Constructor;
import java.util.Map;

public class Instantiator
{
    public static <T> T create(Class<T> type, Map<String, String> args) throws IllegalAccessException, InstantiationException
    {
        try {
            Constructor c = type.getConstructor(Map.class);
            return type.cast(c.newInstance(args));
        }
        catch (Exception e) {}
        return type.newInstance();
    }
}
