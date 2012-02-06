package com.ning.atlas.components.aws;

import org.skife.config.Config;

public abstract class AWSConfig
{
    @Config("aws.access-key")
    public abstract String getAccessKey();

    @Config("aws.secret-key")
    public abstract String getSecretKey();

    @Config("aws.key-name")
    public abstract String getKeyPairId();
}
