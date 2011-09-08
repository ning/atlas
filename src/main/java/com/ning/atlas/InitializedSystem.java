package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class InitializedSystem extends InitializedTemplate
{
    private static final Logger log = Logger.get(InitializedSystem.class);

    private final List<? extends InitializedTemplate> children;

    public InitializedSystem(Identity id, String type, String name, My my, List<? extends InitializedTemplate> children)
    {
        super(id, type, name, my);
        this.children = Lists.newArrayList(children);
    }

    @Override
    public Collection<? extends InitializedTemplate> getChildren()
    {
        return children;
    }

    @Override
    protected ListenableFuture<? extends InstalledElement> install(final ErrorCollector ec, Executor ex, InitializedTemplate root)
    {
        final AtomicInteger remaining = new AtomicInteger(getChildren().size());
        final List<InstalledElement> init_children = new CopyOnWriteArrayList<InstalledElement>();
        final SettableFuture<InstalledElement> rs = SettableFuture.create();
        for (InitializedTemplate template : getChildren()) {
            final ListenableFuture<? extends InstalledElement> child = template.install(ec, ex, root);
            child.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        final InstalledElement ct = child.get();
                        init_children.add(ct);
                        if (remaining.decrementAndGet() == 0) {
                            rs.set(new InstalledSystem(getId(),
                                                       getType(),
                                                       getName(),
                                                       getMy(),
                                                       init_children));
                        }
                    }
                    catch (InterruptedException e) {
                        ec.error(e, "interrupted while running installations");
                    }
                    catch (ExecutionException e) {
                        String msg = ec.error(e.getCause(), "Error while trying to install: %s", e.getCause()
                                                                                                  .getMessage());
                        log.warn(e, msg);
                    }
                }
            }, ex);
        }
        return rs;
    }
}
