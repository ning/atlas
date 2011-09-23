package com.ning.atlas;

import com.ning.atlas.base.Maybe;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class BoundServer extends BoundTemplate
{
    private static final Logger log = Logger.get(BoundServer.class);


    private final Base         base;
    private final List<String> installations;

    public BoundServer(Identity id, String type, String name, My my, Base base, List<String> installations)
    {
        super(id, type, name, my);
        this.base = base;
        this.installations = installations;
    }

    public BoundServer(Identity id,
                       ServerTemplate serverTemplate,
                       String name,
                       Environment env,
                       List<String> installations)
    {
        this(id,
             serverTemplate.getType(),
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
    public Collection<? extends BoundTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Provision> provision(final ErrorCollector collector, ExecutorService e)
    {
        return Provisionater.plan(this, collector);



//        return MoreExecutors.listeningDecorator(e).submit(new Callable<ProvisionedElement>()
//        {
//            public ProvisionedElement call() throws Exception
//            {
//                Threads.pushName("t-" + getId().toExternalForm());
//                try {
//                    Server server = base.getProvisioner().provision(base, BoundServer.this);
//                    return new ProvisionedServer(BoundServer.this, base, server, installations);
//                }
//                catch (Exception e) {
//                    log.error(e, collector.error(e, "unable to provision server %s.%s because of '%s'", getType(), getName(), e
//                        .getMessage()));
//                    return new ProvisionedError(getId(), getType(), getName(), getMy(), e.getMessage());
//                }
//                finally {
//                    Threads.popName();
//                }
//            }
//        });
    }
//
//    @Override
//    public List<Change> upgradeFrom(InstalledElement initialState)
//    {
//        BoundServer prior = findPrior(initialState.getId());
//
//        List<String> prior_inits = prior.getBase().getInits();
//        List<String> my_inits = new ArrayList<String>(this.getBase().getInits());
//        // find inititializations to add and ones to remove
//
//
//        // find installations to add and to remove
//
//
//        return null;
//    }

    private BoundServer findPrior(Identity id)
    {

//         BoundServer bound = new BoundServer(id, getType(), getName(), getMy())

        return null;
    }
}
