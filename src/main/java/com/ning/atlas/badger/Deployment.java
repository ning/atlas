package com.ning.atlas.badger;

public class Deployment
{
    private final SystemMap map;
    private final Space space;

    public Deployment(SystemMap map, Space space) {
        this.map = map;
        this.space = space;
    }

    public static Deployment nil()
    {
        return new Deployment(SystemMap.emptyMap(), Space.emptySpace());
    }
}
