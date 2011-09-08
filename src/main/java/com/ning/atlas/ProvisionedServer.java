package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.ning.atlas.base.Threads;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class ProvisionedServer extends ProvisionedElement
{
    private static final Logger logger = Logger.get(ProvisionedServer.class);

    @JsonIgnore
    private final Server       server;
    private final List<String> installations;
    private final Base         base;

    public ProvisionedServer(Identity id, String type, String name, My my, Server server, List<String> installations, Base base)
    {
        super(id, type, name, my);
        this.server = server;
        this.installations = installations;
        this.base = base;
    }

    public ProvisionedServer(BoundServer boundServerTemplate, Base base, Server server, List<String> installations)
    {
        this(boundServerTemplate.getId(),
             boundServerTemplate.getType(),
             boundServerTemplate.getName(),
             boundServerTemplate.getMy(),
             server,
             installations,
             base);
    }

    @Override
    public List<? extends ProvisionedElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<? extends InitializedTemplate> initialize(final ErrorCollector ec,
                                                                         final Executor ex,
                                                                         final ProvisionedElement root)
    {
        ListenableFutureTask<InitializedTemplate> f =
            new ListenableFutureTask<InitializedTemplate>(new Callable<InitializedTemplate>()
            {
                @Override
                public InitializedTemplate call() throws Exception
                {

                    Threads.pushName("t-" + getId().toExternalForm());
                    try {
                        final Server rs = base.initialize(server, root, ProvisionedServer.this);
                        return new InitializedServer(getId(),
                                                     getType(),
                                                     getName(),
                                                     getMy(),
                                                     rs,
                                                     installations,
                                                     base);
                    }
                    catch (Exception e) {
                        String msg = ec.error(e, "Error while initializing server: %s", e.getMessage());
                        logger.warn(e, msg);
                        return new InitializedError(getId(), getType(), getName(), getMy(), e.getMessage());
                    }
                    finally {
                        Threads.popName();
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
