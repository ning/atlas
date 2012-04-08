package com.ning.atlas.config;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import org.skife.cli.org.iq80.cli.config.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

public class AtlasConfiguration implements Configuration
{
    public static final File ATLAS_CONF    = new File("atlas.conf");
    public static final File RECORDED_CONF = new File(".atlas/.recorded.conf");

    private static final AtlasConfiguration GLOBAL = new AtlasConfiguration(ATLAS_CONF, RECORDED_CONF);

    public static AtlasConfiguration global()
    {
        return GLOBAL;
    }

    private final Map<String, String> atlas    = Maps.newConcurrentMap();
    private final Map<String, String> recorded = Maps.newConcurrentMap();

    private final File record;

    public AtlasConfiguration(File atlas, File recorded)
    {
        record = recorded;
        try {
            if (atlas.exists()) {
                Properties p = new Properties();
                InputStream in = new FileInputStream(atlas);
                p.load(in);
                in.close();
                for (Map.Entry<Object, Object> entry : p.entrySet()) {
                    this.atlas.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }

            if (recorded.exists()) {
                Properties p = new Properties();
                InputStream in = new FileInputStream(recorded);
                p.load(in);
                in.close();
                for (Map.Entry<Object, Object> entry : p.entrySet()) {
                    this.recorded.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void record(String key, String value)
    {
        this.recorded.put(key, value);

        Properties p = new Properties();
        p.putAll(recorded);
        OutputStream out = null;
        try {
            out = new FileOutputStream(record);
            p.store(out, "saving for key " + key);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        finally {
            Closeables.closeQuietly(out);
        }
    }

    @Override
    public Optional<String> lookup(String s)
    {
        if (atlas.containsKey(s)) {
            return Optional.of(atlas.get(s));
        }
        else if (recorded.containsKey(s)) {
            return Optional.of(recorded.get(s));
        }
        else {
            return Optional.absent();
        }
    }
}
