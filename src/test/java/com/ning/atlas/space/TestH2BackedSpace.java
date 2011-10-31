package com.ning.atlas.space;

import com.google.common.io.Files;
import com.ning.atlas.spi.Space;

import java.io.File;
import java.io.IOException;

public class TestH2BackedSpace extends BaseSpaceTest
{
    private File storage;

    @Override
    protected Space createSpace()
    {
        this.storage = Files.createTempDir();
        return H2BackedSpace.create(storage);
    }

    @Override
    protected void destroySpace(Space space) throws IOException
    {
        Files.deleteRecursively(storage);
    }
}
