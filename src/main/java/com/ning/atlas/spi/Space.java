package com.ning.atlas.spi;

import com.ning.atlas.base.Maybe;
import com.ning.atlas.space.Missing;

/**
 * @todo Add garbage collection via mark and sweep (marked if read or written during a deployment, sweep at end)
 */
public interface Space
{
    void store(Identity id, Object it);

    void scratch(String key, String value);

    Maybe<String> get(String key);

    <T> Maybe<T> get(Identity id, Class<T> type, Missing behavior);
}
