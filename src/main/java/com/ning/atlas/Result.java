package com.ning.atlas;

import com.ning.atlas.space.InMemorySpace;
import com.ning.atlas.spi.Space;

public class Result
{
    private final SystemMap map;
    private final Space space;

    public Result(SystemMap map, Space space) {
        this.map = map;
        this.space = space;
    }

    public static Result nil()
    {
        return new Result(SystemMap.emptyMap(), InMemorySpace.newInstance());
    }
}
