package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.upgrade.UpgradePlan;
import com.ning.atlas.upgrade.UpgradeSystemPlan;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.*;

public class BoundSystemTemplate extends BoundTemplate
{
    private final List<BoundTemplate> children;
    private static final Logger logger = Logger.get(BoundSystemTemplate.class);

    /**
     * All other ctors MUST delegate to thi one, it is canonical
     */
    public BoundSystemTemplate(Identity id, String type, String name, My my, Iterable<? extends BoundTemplate> children)
    {
        super(id, type, name, my);
        this.children = Lists.newArrayList();
        addAll(this.children, children);
    }

    /**
     * ctor exists for use during dev, ideally should be removed and use the main ctor
     */
    public BoundSystemTemplate(final Identity id, SystemTemplate sys, String name, final Environment env)
    {
        this(id, sys.getType(), name, sys.getMy(), concat(transform(sys.getChildren(),
                                                                new Function<Template, Iterable<BoundTemplate>>()
                                                                {
                                                                    public Iterable<BoundTemplate> apply(Template in)
                                                                    {
                                                                        return in._normalize(env, id);
                                                                    }
                                                                })));
    }

    @Override
    public List<? extends BoundTemplate> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ListenableFuture<? extends ProvisionedElement> provision(final ErrorCollector collector, Executor exec)
    {
        final SettableFuture<ProvisionedElement> f = SettableFuture.create();

        final CopyOnWriteArrayList<ProvisionedElement> p_children = new CopyOnWriteArrayList<ProvisionedElement>();
        final AtomicInteger remaining = new AtomicInteger(children.size());

        for (final BoundTemplate child : children) {
            final ListenableFuture<? extends ProvisionedElement> cf = child.provision(collector, exec);
            cf.addListener(new Runnable()
            {
                public void run()
                {
                    try {
                        ProvisionedElement pt = cf.get();
                        p_children.add(pt);

                    }
                    catch (InterruptedException e) {
                        collector.error(e, "interrupted while waiting on children to finish provisioning");
                        Thread.currentThread().interrupt();
                    }
                    catch (ExecutionException e) {
                        String msg = collector.error(e.getCause(), "error while provisioning child %s", child);
                        logger.error(e, msg);
                    }
                    finally {
                        if (remaining.decrementAndGet() == 0) {
                            f.set(new ProvisionedSystem(getId(),
                                                        getType(),
                                                        getName(),
                                                        getMy(),
                                                        p_children));
                        }
                    }
                }
            }, MoreExecutors.sameThreadExecutor());
        }

        return f;
    }

    @Override
    public UpgradePlan upgradeFrom(InstalledElement initialState)
    {
        List<UpgradePlan> plan_children = Lists.newArrayList();
        for (BoundTemplate child : children) {
            plan_children.add(child.upgradeFrom(initialState));
        }
        return new UpgradeSystemPlan(plan_children);
    }
}
