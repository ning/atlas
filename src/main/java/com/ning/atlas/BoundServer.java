package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.upgrade.UpgradePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

public class BoundServer extends BoundTemplate
{
    private static final Logger log = LoggerFactory.getLogger(BoundServer.class);

    private final Base base;
    private final List<String> installations;

    public BoundServer(String type, String name, My my, Base base, List<String> installations)
    {
        super(type, name, my);
        this.base = base;
        this.installations = installations;
    }

    public BoundServer(ServerTemplate serverTemplate,
                       String name,
                       Environment env,
                       List<String> installations)
    {
        this(serverTemplate.getType(),
             name,
             serverTemplate.getMy(),
             extractBase(serverTemplate, env),
             installations);
    }

    public List<String> getInstallations()
    {
        return installations;
    }

    private static Base extractBase(ServerTemplate serverTemplate, Environment env)
    {
        Maybe<Base> mb = env.findBase(serverTemplate.getBase());
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
    public ListenableFuture<ProvisionedElement> provision(Executor e)
    {
        final ListenableFutureTask<ProvisionedElement> f =
            new ListenableFutureTask<ProvisionedElement>(new Callable<ProvisionedElement>()
            {
                public ProvisionedElement call() throws Exception
                {
                    try {
                        Server server = base.getProvisioner().provision(base);
                        return new ProvisionedServer(BoundServer.this, base, server, installations);
                    }
                    catch (Exception e) {
                        log.warn("unable to provision server {}", getType() + "." + getName(), e);
                        return new ProvisionedError(getType(), getName(), getMy(), e.getMessage());
                    }
                }
            });
        e.execute(f);
        return f;
    }

    @Override
    public UpgradePlan upgradeFrom(InstalledElement initialState)
    {

        List<String> id = Lists.newArrayList(getType(), getName());

        BoundServer prior = findPrior(id);

        List<String> prior_inits = prior.getBase().getInits();
        List<String> my_inits = new ArrayList<String>(this.getBase().getInits());
        // find inititializations to add and ones to remove


        // find installations to add and to remove



        return null;
    }

    private BoundServer findPrior(List<String> id)
    {
        return null;
    }
}
