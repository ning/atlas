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

public class InitializedServerTemplate extends InitializedTemplate
{
    @JsonIgnore
    private final Server server;
    private final List<String> installations;

    public InitializedServerTemplate(String type, String name, My my, Server server, List<String> installations)
    {
        super(type, name, my);
        this.server = server;
        this.installations = installations;
    }

    @JsonIgnore
    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<? extends InstalledTemplate> install(Executor exec, final InitializedTemplate root)
    {
        ListenableFutureTask<InstalledTemplate> f =
            new ListenableFutureTask<InstalledTemplate>(new Callable<InstalledTemplate>()
            {
                @Override
                public InstalledTemplate call() throws Exception
                {
                    try {
                        for (String installation : installations) {
                            int offset = installation.indexOf(':');
                            String prefix = installation.substring(0, offset);
                            String fragment = installation.substring(offset + 1, installation.length());

                            Installer installer = server.getBase().getInstaller(prefix);
                            installer.install(server, fragment, root);
                        }

                        return new InstalledServerTemplate(getType(), getName(), getMy(), server);
                    }
                    catch (Exception e) {
                        return new InstalledErrorTemplate(getType(),  getName(), getMy(), e);
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
        return this.server.getEnvironmentProperties();
    }
}
