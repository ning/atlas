package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.ning.atlas.base.Maybe;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class BoundServerTemplate extends BoundTemplate
{
    private final Base base;
    private final List<String> installations;

    public BoundServerTemplate(String type, String name, My my, Base base, List<String> installations)
    {
        super(type, name, my);
        this.base = base;
        this.installations = installations;
    }

    public BoundServerTemplate(ServerTemplate serverTemplate,
                               String name,
                               Environment env,
                               Stack<String> names,
                               List<String> installations)
    {
        this(serverTemplate.getType(),
             name,
             serverTemplate.getMy(),
             extractBase(serverTemplate, env, names),
             installations);
    }

    private static Base extractBase(ServerTemplate serverTemplate, Environment env, Stack<String> names)
    {
        Maybe<Base> mb = env.findBase(serverTemplate.getBase(), names);
        if (mb.isKnown()) {
            return mb.getValue();
        }
        else {
            throw new IllegalStateException("No base named '" + serverTemplate.getBase() + "' found!");
        }

    }

    public Base getBase()
    {
        return base;
    }

    @Override
    public List<BoundTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<ProvisionedTemplate> provision(Executor e)
    {
        final ListenableFutureTask<ProvisionedTemplate> f =
            new ListenableFutureTask<ProvisionedTemplate>(new Callable<ProvisionedTemplate>()
            {
                public ProvisionedTemplate call() throws Exception
                {
                    try {
                        Server server = base.getProvisioner().provision(base);
                        return new ProvisionedServerTemplate(BoundServerTemplate.this, server, installations);
                    }
                    catch (UnableToProvisionServerException e) {
                        return new ProvisionedErrorTemplate(getType(), getName(), getMy(), e.getMessage());
                    }
                }
            });
        e.execute(f);
        return f;
    }
}
