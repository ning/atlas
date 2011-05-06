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
    private final Base        base;
    private final Provisioner provisioner;

    public BoundServerTemplate(String name, Base base, Provisioner provisioner)
    {
        super(name);
        this.base = base;
        this.provisioner = provisioner;
    }

    public BoundServerTemplate(ServerTemplate serverTemplate, Environment env, Stack<String> names)
    {
        this(serverTemplate.getName(),
             extractBase(env.findBase(serverTemplate.getBase(), names), serverTemplate.getBase()),
             env.getProvisioner());
    }

    private static Base extractBase(Maybe<Base> base, String name)
    {
        if (base.isKnown()) {
            return base.otherwise(new Base("WAFFLES"));
        }
        else {
            throw new IllegalArgumentException("Unable to locate base '" + name + "' in environment");
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
                    Server server = provisioner.provision(base);
                    return new ProvisionedServerTemplate(BoundServerTemplate.this, server);
                }
            });
        e.execute(f);
        return f;
    }
}
