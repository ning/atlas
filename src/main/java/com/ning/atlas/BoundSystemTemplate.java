package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.synchronizedList;

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

        final CountDownLatch latch = new CountDownLatch(children.size());

        final SettableFuture<ProvisionedSystemTemplate> rs = SettableFuture.create();
        final List<ProvisionedTemplate> pc = synchronizedList(new ArrayList<ProvisionedTemplate>(children.size()));
        for (BoundTemplate child : children) {
            final ListenableFuture<? extends ProvisionedTemplate> t = child.provision(exec);
            t.addListener(new ListenableFutureTask<Void>(new Callable<Void>()
            {
                public Void call() throws Exception
                {

                    pc.add(t.get());
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        rs.set(new ProvisionedSystemTemplate(getName(), pc));
                    }

                    return null;
                }
            }), exec);

        }

        return rs;
    }
}
