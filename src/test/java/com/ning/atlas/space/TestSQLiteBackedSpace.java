package com.ning.atlas.space;

import com.ning.atlas.spi.space.Space;

import java.io.File;
import java.io.IOException;

public class TestSQLiteBackedSpace extends BaseSpaceTest
{
    private File tmp;

    @Override
    protected Space createSpace() throws IOException
    {
        this.tmp = File.createTempFile("atlas", ".db");
        return SQLiteBackedSpace.create(tmp);
    }

    @Override
    protected void destroySpace(Space space) throws IOException
    {
        tmp.delete();
        // NOOP
    }
}
