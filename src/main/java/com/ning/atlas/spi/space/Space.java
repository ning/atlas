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

    Maybe<String> get(Identity id, String key);
    Maybe<String> get(String idExternalForm, String key);

    <T> Maybe<T> get(Identity id, Class<T> type, Missing behavior);

    Map<SpaceKey, String> getAllFor(Identity id);

    Set<Identity> findAllIdentities();

    void deleteAll(Identity identity);

    <T> Maybe<T> get(Identity id, Class<T> type);

    void delete(Identity identity, String key);

    Set<String> query(String expression);
}
