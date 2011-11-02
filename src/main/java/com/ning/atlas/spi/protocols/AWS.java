package com.ning.atlas.spi.protocols;

import com.ning.atlas.spi.Identity;

import java.util.concurrent.atomic.AtomicReference;

public class AWS
{
    public static final Identity ID = Identity.root().createChild("atlas", "private").createChild("config", "aws");

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
    }

    public static class SSHKeyPairInfo
    {
        private AtomicReference<String> name        = new AtomicReference<String>();
        private AtomicReference<String> keyPairFile = new AtomicReference<String>();

        public String getId()
        {
            return name.get();
        }

        public void getId(String name)
        {
            this.name.set(name);
        }

        public String getFile()
        {
            return keyPairFile.get();
        }

        public void setFile(String keyPairFile)
        {
            this.keyPairFile.set(keyPairFile);
        }
    }
}
