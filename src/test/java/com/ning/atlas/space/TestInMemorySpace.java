package com.ning.atlas.space;

import com.ning.atlas.spi.space.Space;

import java.io.IOException;

public class TestInMemorySpace extends BaseSpaceTest
{
    @Override
    protected Space createSpace()
    {
        return InMemorySpace.newInstance();
    }

    @Override
    protected void destroySpace(Space space) throws IOException
    {
    }
}
