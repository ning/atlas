package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.ning.atlas.base.ListenableExecutorService;
import com.ning.atlas.base.Threads;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class InitializedServer extends InitializedTemplate
{
    private static final Logger log = Logger.get(InitializedServer.class);

    @JsonIgnore
    private final Server       server;
    private final List<String> installations;
    private final Base         base;

    public InitializedServer(Identity id, String type, String name, My my, Server server, List<String> installations, Base base)
    {
        super(id, type, name, my);
        this.server = server;
        this.installations = installations;
        this.base = base;
    }

    @JsonIgnore
    @Override
    public Collection<? extends Thing> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<InstalledElement> install(final ErrorCollector ec, ExecutorService exec, final InitializedTemplate root)
    {
        return ListenableExecutorService.delegateTo(exec).submit(new Callable<InstalledElement>()
        {
            @Override
            public InstalledElement call() throws Exception
            {
                Threads.pushName("t-" + getId().toExternalForm());
                try {
                    for (String installation : installations) {
                        int offset = installation.indexOf(':');
                        final String prefix, fragment;
                        if (offset == -1) {
                            prefix = installation;
                            fragment = "";
                        }
                        else {
                            prefix = installation.substring(0, offset);
                            fragment = installation.substring(offset + 1, installation.length());
                        }
                        Installer installer = InitializedServer.this.base.getInstaller(prefix);
                        installer.install(server, fragment, root, InitializedServer.this);
                    }

                    return new InstalledServer(getId(), getType(), getName(), getMy(), server, base.getProperties());
                }
                catch (Exception e) {
                    String msg = ec.error(e, "Error while attempting to run installations on server: %s",
                                          e.getMessage());
                    log.warn(e, msg);
                    return new InstalledError(getId(), getType(), getName(), getMy(), e);
                }
                finally {
                    Threads.popName();
                }
            }
        });
    }

    @JsonIgnore
    public Server getServer()
    {
        return server;
    }

    @JsonProperty("environment")
    public Map<String, String> getEnvironmentProperties()
    {
        return this.base.getProperties();
    }
}
