package com.ning.atlas.spi;

public interface Scratch
{
    public void put(String key, String value);

    public Maybe<String> get(String key);
}
