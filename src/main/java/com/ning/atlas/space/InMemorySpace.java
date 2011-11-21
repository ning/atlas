package com.ning.atlas.space;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.SpaceKey;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class InMemorySpace extends BaseSpace
{

    private final ConcurrentMap<SpaceKey, String> values = new MapMaker().concurrencyLevel(2).makeMap();

    public static InMemorySpace newInstance()
    {
        return new InMemorySpace();
    }

    @Override
    protected String read(Identity id, String key) throws IOException
    {
        return values.get(SpaceKey.from(id, key));
    }

    @Override
    protected void write(Identity id, String key, String value) throws IOException
    {
        values.put(SpaceKey.from(id, key), value);
    }

    @Override
    protected Map<SpaceKey, String> readAll(Identity prefix) throws IOException
    {
        Map<SpaceKey, String> rs = Maps.newHashMap();
        for (Map.Entry<SpaceKey, String> entry : values.entrySet()) {
            if (prefix.equals(entry.getKey().getIdentity()) || prefix.isParentOf(entry.getKey().getIdentity())) {
                rs.put(entry.getKey(), entry.getValue());
            }
        }
        return rs;
    }

    @Override
    public Set<Identity> findAllIdentities()
    {
        Set<Identity> rs = Sets.newHashSet();
        for (SpaceKey key : values.keySet()) {
            rs.add(key.getIdentity());
        }
        return rs;
    }

    @Override
    public void deleteAll(Identity identity)
    {
        Set<SpaceKey> keys = Sets.newHashSet();
        for (SpaceKey key : values.keySet()) {
            if (identity.equals(key.getIdentity())) {
                keys.add(key);
            }
        }
        for (SpaceKey key : keys) {
            values.remove(key);
        }
    }

    @Override
    public void delete(Identity identity, String key)
    {
        values.remove(SpaceKey.from(identity, key));
    }
}
