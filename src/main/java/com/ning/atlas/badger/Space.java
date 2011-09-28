package com.ning.atlas.badger;

/**
 * @todo Add garbage collection via mark and sweep (marked if read or written during a deployment, sweep at end)
 */
public class Space
{
    private Space() {

    }

    public static Space emptySpace()
    {
        return new Space();
    }
}
