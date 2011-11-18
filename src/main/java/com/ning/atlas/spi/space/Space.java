package com.ning.atlas.spi.space;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;

import java.util.Map;
import java.util.Set;

/**
 * @todo Add garbage collection via mark and sweep (marked if read or written during a deployment, sweep at end)
 */
public interface Space
{
    void store(Identity id, Object it);
    void store(Identity id, String key, String value);

    void scratch(String key, String value);

    Maybe<String> get(String key);
    Maybe<String> get(Identity id, String key);
    <T> Maybe<T> get(Identity id, Class<T> type, Missing behavior);

    /**
     * Will raise an IllegalStateException if s is not available and cannot be made available in the future.
     *
     * @param s the key
     * @return the value
     */
    String require(String s);

    Map<SpaceKey, String> getAllFor(Identity id);

    Set<Identity> findAllIdentities();

    void deleteAll(Identity identity);

    <T> Maybe<T> get(Identity id, Class<T> type);
}
