package com.ning.atlas;

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
        return new Result(SystemMap.emptyMap(), Space.emptySpace());
    }
}
