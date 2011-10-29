package com.ning.atlas.space;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.ning.atlas.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class DiskBackedSpace extends BaseSpace
{
    private static final Logger log = Logger.get(DiskBackedSpace.class);
    private final File storageDir;

    private DiskBackedSpace(File storageDir)
    {
        checkArgument(storageDir.isDirectory() || !storageDir.exists(), "storageDir must be a directory or not exist");

        log.info("storing data in %s", storageDir.getAbsolutePath());
        this.storageDir = storageDir;
    }

    public static DiskBackedSpace create(File storageDirectory)
    {
        return new DiskBackedSpace(storageDirectory);
    }

    @Override
    protected String read(String key) throws IOException
    {
        File f = new File(storageDir, key);
        if (!f.exists()) return null;

        return Files.readLines(f, Charset.forName("UTF8"), new LineProcessor<String>()
        {
            final StringBuffer buf = new StringBuffer();

            @Override
            public boolean processLine(String line) throws IOException
            {
                buf.append(line);
                return line != null;
            }

            @Override
            public String getResult()
            {
                return buf.toString();
            }
        });
    }

    @Override
    protected void write(String key, String value) throws IOException
    {
        Files.write(value.getBytes(Charset.forName("UTF8")), new File(storageDir, key));
    }

    @Override
    protected Map<String, String> readAll(String prefix) throws IOException
    {
        Map<String, String> rs = Maps.newHashMap();
        for (File file : storageDir.listFiles()) {
            if (file.isFile() && file.getName().startsWith(prefix) ) {
                rs.put(file.getName(), read(file.getName()));
            }
        }
        return rs;
    }
}
