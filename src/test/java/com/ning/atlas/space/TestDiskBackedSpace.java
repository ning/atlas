package com.ning.atlas.space;

import com.google.common.io.Files;
import com.ning.atlas.spi.Space;

import java.io.File;
import java.io.IOException;

public class TestDiskBackedSpace extends BaseSpaceTest
{
    private File tmpDir;

    @Override
    protected Space createSpace()
    {
        this.tmpDir = Files.createTempDir();
        return DiskBackedSpace.create(tmpDir);
    }

    @Override
    protected void destroySpace(Space space) throws IOException
    {
        Files.deleteRecursively(tmpDir);
    }
}
