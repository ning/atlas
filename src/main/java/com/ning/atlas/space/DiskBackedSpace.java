package com.ning.atlas.space;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.spi.Identity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class DiskBackedSpace extends BaseSpace
{
    private static final Logger log = Logger.get(DiskBackedSpace.class);
    private final File storageDir;

    private DiskBackedSpace(File storageDir)
    {
        checkArgument(storageDir.isDirectory() || !storageDir.exists(), "storageDir must be a directory or not exist");

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                throw new IllegalStateException("unable to create storage dir " + storageDir.getAbsolutePath());
            }
        }

        log.info("storing data in %s", storageDir.getAbsolutePath());
        this.storageDir = storageDir;
    }

    public static DiskBackedSpace create(File storageDirectory)
    {
        return new DiskBackedSpace(storageDirectory);
    }

    @Override
    protected String read(Identity id, String key) throws IOException
    {
        File dir = new File(storageDir, munge(id.toExternalForm()));
        if (!dir.exists()) { return null; }

        File f = new File(dir, key);
        if (!f.exists()) return null;

        return readFile(f);
    }

    private String munge(String key)
    {
        return key.replaceAll("/", "_");
    }

    @Override
    protected void write(Identity id, String key, String value) throws IOException
    {
        File dir = new File(storageDir, munge(id.toExternalForm()));
        if (!dir.exists()) { if (!dir.mkdirs()) { throw new IOException("unable to make storage dir"); } }

        Files.write(value.getBytes(Charset.forName("UTF8")), new File(dir, key));
    }

    @Override
    protected Map<String, String> readAll(Identity id) throws IOException
    {
        File dir = new File(storageDir, munge(id.toExternalForm()));
        if (!dir.exists()) { return Collections.emptyMap(); }

        Map<String, String> rs = Maps.newHashMap();
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                rs.put(file.getName(), readFile(file));
            }
        }
        return rs;
    }

    private String readFile(File f) throws IOException
    {
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
}
