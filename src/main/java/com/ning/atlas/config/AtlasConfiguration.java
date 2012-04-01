package com.ning.atlas.config;

import com.google.common.base.Optional;
import org.skife.cli.org.iq80.cli.config.Configuration;

public class AtlasConfiguration implements Configuration
{

    private static final AtlasConfiguration GLOBAL = new AtlasConfiguration();

    public static AtlasConfiguration global()
    {
        return GLOBAL;
    }

    private AtlasConfiguration()
    {

    }

    @Override
    public Optional<String> lookup(String s)
    {
        return null;
    }
}
