package com.ning.atlas.ec2;

import org.skife.config.Config;

import java.io.File;

public abstract class AWSConfig
{
    @Config("aws.access-key")
    public abstract String getAccessKey();

    @Config("aws.secret-key")
    public abstract String getSecretKey();

    @Config("aws.key-name")
    public abstract String getKeyPairId();
}
