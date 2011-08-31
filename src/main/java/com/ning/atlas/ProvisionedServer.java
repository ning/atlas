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

public class ProvisionedServer extends ProvisionedElement
{
    @JsonIgnore
    private final Server       server;
    private final List<String> installations;
    private final Base         base;

    public ProvisionedServer(String type, String name, My my, Server server, List<String> installations, Base base)
    {
        super(type, name, my);
        this.server = server;
        this.installations = installations;
        this.base = base;
    }

    public ProvisionedServer(BoundServer boundServerTemplate, Base base, Server server, List<String> installations)
    {
        this(boundServerTemplate.getType(),
             boundServerTemplate.getName(),
             boundServerTemplate.getMy(),
             server,
             installations,
             base);
    }

    @JsonIgnore
    public List<? extends ProvisionedElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<? extends InitializedTemplate> initialize(Executor ex, final ProvisionedElement root)
    {
        ListenableFutureTask<InitializedTemplate> f =
            new ListenableFutureTask<InitializedTemplate>(new Callable<InitializedTemplate>()
            {
                @Override
                public InitializedTemplate call() throws Exception
                {

                    try {
                        final Server rs = base.initialize(server, root, ProvisionedServer.this);
                        return new InitializedServer(getType(),
                                                     getName(),
                                                     getMy(),
                                                     rs,
                                                     installations,
                                                     base);
                    }
                    catch (Exception e) {
                        return new InitializedError(getType(), getName(), getMy(), e.getMessage());
                    }
                }
            });

        ex.execute(f);
        return f;
    }

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
