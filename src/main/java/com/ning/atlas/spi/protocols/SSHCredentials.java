package com.ning.atlas.spi.protocols;

import com.ning.atlas.space.Missing;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.Space;

import java.util.concurrent.atomic.AtomicReference;

public class SSHCredentials
{
    private AtomicReference<String> keyFilePath = new AtomicReference<String>();
    private AtomicReference<String> userName = new AtomicReference<String>();
    public static String DEFAULT_CREDENTIAL_NAME = "default";

    public String getKeyFilePath() {
        return keyFilePath.get();
    }
    public String getUserName() {
        return userName.get();
    }

    public void setKeyFilePath(String keyPairFile)
    {
        this.keyFilePath.set(keyPairFile);
    }

    public void setUserName(String userName)
    {
        this.userName.set(userName);
    }

    private static Identity createId(String name) {
        return Identity.root().createChild("atlas", "private").createChild("credentials", name);
    }

    public static void store(Space space, SSHCredentials credentials, String name) {
        space.store(createId(name), credentials);
    }

    public static Maybe<SSHCredentials> lookup(Space space, String name) {
        if (name == null) {
            return Maybe.unknown();
        }
        Identity id = createId(name);
        return space.get(id, SSHCredentials.class, Missing.RequireAll);
    }

    public static Maybe<SSHCredentials> defaultCredentials(Space space) {
        return lookup(space, DEFAULT_CREDENTIAL_NAME);
    }
}
