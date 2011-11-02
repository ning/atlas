package com.ning.atlas;

import com.ning.atlas.logging.Logger;

import java.lang.reflect.Constructor;
import java.util.Map;

public class Instantiator
{
    private static final Logger log = Logger.get(Instantiator.class);
    public static <T> T create(Class<T> type, Map<String, String> args) throws IllegalAccessException, InstantiationException
    {
        try {
            Constructor c = type.getConstructor(Map.class);
            return type.cast(c.newInstance(args));
        }
        catch (NoSuchMethodException e) {
            // NOOP
        }
        catch (Exception e) {
            log.warn(e, "exception trying to use map constructor on " + type.getName());
        }
        return type.newInstance();
    }
}
