package com.ning.atlas.spi.protocols;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Core;

import java.util.concurrent.atomic.AtomicReference;

public class AWS
{
    public static final Identity ID = Core.ID.createChild("aws", "config");

    public static class Credentials
    {
        private AtomicReference<String> accessKey = new AtomicReference<String>();
        private AtomicReference<String> secretKey = new AtomicReference<String>();

        public String getAccessKey()
        {
            return accessKey.get();
        }

        public void setAccessKey(String accessKey)
        {
            this.accessKey.set(accessKey);
        }

        public String getSecretKey()
        {
            return secretKey.get();
        }

        public void setSecretKey(String secretKey)
        {
            this.secretKey.set(secretKey);
        }

        public AWSCredentials toAWSCredentials()
        {
            return new BasicAWSCredentials(this.getAccessKey(), this.getSecretKey());
        }
    }

    public static class SSHKeyPairInfo
    {
        private AtomicReference<String> name        = new AtomicReference<String>();
        private AtomicReference<String> keyPairFile = new AtomicReference<String>();

        public String getKeyPairId()
        {
            return name.get();
        }

        public void setKeyPairId(String name)
        {
            this.name.set(name);
        }

        public String getPrivateKeyFile()
        {
            return keyPairFile.get();
        }

        public void setPrivateKeyFile(String keyPairFile)
        {
            this.keyPairFile.set(keyPairFile);
        }
    }
}
