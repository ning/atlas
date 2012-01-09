package com.ning.atlas;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Scratch;

import java.util.List;

public class ActualScratch implements Scratch
{
    private static final Logger log = Logger.get(ActualScratch.class);

    private final ListMultimap<String, String> values = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, String>create());

    public void put(String key, String value)
    {
        synchronized (values) {
            log.info("%s = %s", key, value);
            values.put(key, value);
        }
    }

    public Maybe<String> get(String key)
    {
        synchronized (values) {
            if (values.get(key).isEmpty()) {
                return Maybe.unknown();
            }
            else {
                List<String> all = values.get(key);
                String rs = all.get(all.size() - 1); // return the last if asking for one value
                return Maybe.definitely(rs);
            }
        }
    }

    public List<String> getAllFor(String key) {
        synchronized (values) {
            return ImmutableList.copyOf(values.get(key));
        }
    }

}
