package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class InitializedServer extends InitializedTemplate
{
    @JsonIgnore
    private final Server server;
    private final List<String> installations;
    private final Base base;

    public InitializedServer(String type, String name, My my, Server server, List<String> installations, Base base)
    {
        super(type, name, my);
        this.server = server;
        this.installations = installations;
        this.base = base;
    }

    @JsonIgnore
    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<? extends InstalledElement> install(Executor exec, final InitializedTemplate root)
    {
        ListenableFutureTask<InstalledElement> f =
            new ListenableFutureTask<InstalledElement>(new Callable<InstalledElement>()
            {
                @Override
                public InstalledElement call() throws Exception
                {
                    try {
                        for (String installation : installations) {
                            int offset = installation.indexOf(':');
                            String prefix = installation.substring(0, offset);
                            String fragment = installation.substring(offset + 1, installation.length());

                            Installer installer = InitializedServer.this.base.getInstaller(prefix);
                            installer.install(server, fragment, root);
                        }

                        return new InstalledServer(getType(), getName(), getMy(), server, base.getProperties());
                    }
                    catch (Exception e) {
                        return new InstalledError(getType(),  getName(), getMy(), e);
                    }
                }
            });
        exec.execute(f);
        return f;
    }

    @JsonIgnore
    public Server getServer()
    {
        return server;
    }

    @JsonProperty("environment")
    public Map<String, String> getEnvironmentProperties() {
        return this.base.getProperties();
    }
}
