package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
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


    /**
     * All other ctors MUST delegate to thi one, it is canonical
     */
    public BoundSystemTemplate(String type, String name, My my, Iterable<? extends BoundTemplate> children)
    {
        super(type, name, my);
        this.children = Lists.newArrayList();
        addAll(this.children, children);
    }

    /**
     * ctor exists for use during dev, ideally should be removed and use the main ctor
     */
    public BoundSystemTemplate(SystemTemplate systemTemplate, String name, final Environment env)
    {
        this(systemTemplate.getType(), name, systemTemplate.getMy(), concat(transform(systemTemplate.getChildren(),
                                                                                      new Function<Template, Iterable<BoundTemplate>>()
                                                                                      {
                                                                                          public Iterable<BoundTemplate> apply(Template in)
                                                                                          {
                                                                                              return in._normalize(env);
                                                                                          }
                                                                                      })));
    }

    @Override
    public List<BoundTemplate> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ListenableFuture<? extends ProvisionedElement> provision(Executor exec)
    {
        final SettableFuture<ProvisionedElement> f = SettableFuture.create();

        final CopyOnWriteArrayList<ProvisionedElement> p_children = new CopyOnWriteArrayList<ProvisionedElement>();
        final AtomicInteger remaining = new AtomicInteger(children.size());

        for (BoundTemplate child : children) {
            final ListenableFuture<? extends ProvisionedElement> cf = child.provision(exec);
            cf.addListener(new Runnable()
            {
                public void run()
                {
                    try {
                        ProvisionedElement pt = cf.get();
                        p_children.add(pt);
                        if (remaining.decrementAndGet() == 0) {
                            f.set(new ProvisionedSystem(getType(),
                                                        getName(),
                                                        getMy(),
                                                        p_children));
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    catch (ExecutionException e) {
                        e.printStackTrace();
                        System.exit(1);
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
