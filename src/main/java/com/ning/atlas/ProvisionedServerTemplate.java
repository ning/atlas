package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class ProvisionedServerTemplate extends ProvisionedTemplate
{
    @JsonIgnore
    private final Server server;
    private final List<String> installations;


    public ProvisionedServerTemplate(String type, String name, My my, Server server, List<String> installations)
    {
        super(type, name, my);
        this.server = server;
        this.installations = installations;
    }

    public ProvisionedServerTemplate(BoundServerTemplate boundServerTemplate, Server server, List<String> installations)
    {
        this(boundServerTemplate.getType(),
             boundServerTemplate.getName(),
             boundServerTemplate.getMy(),
             server,
             installations);
    }

    @JsonIgnore
    public List<? extends ProvisionedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<? extends InitializedTemplate> initialize(Executor ex, final ProvisionedTemplate root)
    {
        ListenableFutureTask<InitializedTemplate> f =
            new ListenableFutureTask<InitializedTemplate>(new Callable<InitializedTemplate>()
            {
                @Override
                public InitializedTemplate call() throws Exception
                {

                    try {
                        final Server rs = server.getBase().initialize(server, root);
                        return new InitializedServerTemplate(getType(),
                                                             getName(),
                                                             getMy(),
                                                             rs,
                                                             installations);
                    }
                    catch (Exception e) {
                        return new InitializedErrorTemplate(getType(), getName(), getMy(), e.getMessage());
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
}
