package com.ning.atlas.oracle;

import com.ning.atlas.InitializedTemplate;
import com.ning.atlas.Installer;
import com.ning.atlas.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleLoaderInstaller implements Installer
{
    private final Logger log = LoggerFactory.getLogger(OracleLoaderInstaller.class);

    @Override
    public Server install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
        log.warn("this doesn;t do anything yet!");
        return server;
    }
}
