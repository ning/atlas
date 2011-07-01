package com.ning.atlas.base;

import org.skife.config.ConfigSource;

import java.util.Map;

public class MapConfigSource implements ConfigSource
{

    private final Map<String, String> map;

    public MapConfigSource(Map<String, String> map)
    {
        this.map = map;
    }

    @Override
    public String getString(String propertyName)
    {
        return map.get(propertyName);
    }
}
