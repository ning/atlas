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

    public BoundServerTemplate(String name, Base base)
    {
        super(name);
        this.base = base;
    }

    public BoundServerTemplate(ServerTemplate serverTemplate, Environment env, Stack<String> names)
    {
        this(serverTemplate.getName(), extractBase(serverTemplate, env, names));
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
    public ListenableFuture<ProvisionedServerTemplate> provision(Executor e)
    {
        final ListenableFutureTask<ProvisionedServerTemplate> f =
            new ListenableFutureTask<ProvisionedServerTemplate>(new Callable<ProvisionedServerTemplate>()
            {
                public ProvisionedServerTemplate call() throws Exception
                {
                    Server server = base.getProvisioner().provision(base);
                    return new ProvisionedServerTemplate(BoundServerTemplate.this, server);
                }
            });
        e.execute(f);
        return f;
    }
}
