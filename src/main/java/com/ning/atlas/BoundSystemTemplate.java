package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

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
    public BoundSystemTemplate(String name, Iterable<? extends BoundTemplate> children)
    {
        super(name);
        this.children = Lists.newArrayList();
        addAll(this.children, children);
    }

    /**
     * ctor exists for use during dev, ideally should be removed and use the main ctor
     */
    public BoundSystemTemplate(SystemTemplate systemTemplate, final Environment env, final Stack<String> names)
    {
        this(systemTemplate.getName(), concat(transform(systemTemplate.getChildren(),
                                                        new Function<Template, Iterable<BoundTemplate>>()
                                                        {
                                                            public Iterable<BoundTemplate> apply(Template input)
                                                            {
                                                                return input.normalize(env, names);
                                                            }
                                                        })));
    }

    @Override
    public List<BoundTemplate> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ListenableFuture<? extends ProvisionedTemplate> provision(Executor exec)
    {
        final SettableFuture<ProvisionedTemplate> f = SettableFuture.create();

        final CopyOnWriteArrayList<ProvisionedTemplate> p_children = new CopyOnWriteArrayList<ProvisionedTemplate>();
        final AtomicInteger remaining = new AtomicInteger(children.size());

        for (BoundTemplate child : children) {
            final ListenableFuture<? extends ProvisionedTemplate> cf = child.provision(exec);
            cf.addListener(new Runnable()
            {
                public void run()
                {
                    try {
                        ProvisionedTemplate pt =  cf.get();
                        p_children.add(pt);
                        if (remaining.decrementAndGet() == 0) {
                            f.set(new ProvisionedSystemTemplate(getName(), p_children));
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
}
