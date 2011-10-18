package com.ning.atlas.spi;

import com.ning.atlas.SystemMap;

import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseComponent implements Component
{
    private final AtomicReference<SystemMap> systemMap = new AtomicReference<SystemMap>();
    private final AtomicReference<Space> space = new AtomicReference<Space>();

    protected Space getSpace() {
        return space.get();
    }

    protected SystemMap getSystemMap() {
        return systemMap.get();
    }

    @Override
    public final void start(SystemMap map, Space space)
    {
        this.systemMap.set(map);
        this.space.set(space);
        startLocal(map, space);
    }

    protected void startLocal(SystemMap map, Space space) {}

    @Override
    public final void finish(SystemMap map, Space space)
    {
        this.systemMap.set(null);
        this.space.set(null);
        finishLocal(map, space);
    }

    protected void finishLocal(SystemMap map, Space space) {}
}
