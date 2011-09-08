package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ProvisionedSystem extends ProvisionedElement
{
    private final static Logger logger = Logger.get(ProvisionedSystem.class);

    private final List<? extends ProvisionedElement> children;

    public ProvisionedSystem(Identity id, String type, String name, My my, List<? extends ProvisionedElement> children)
    {
        super(id, type, name, my);
        this.children = new ArrayList<ProvisionedElement>(children);
    }

    public List<? extends ProvisionedElement> getChildren()
    {
        return children;
    }

    @Override
    protected ListenableFuture<InitializedTemplate> initialize(final ErrorCollector ec, Executor ex, ProvisionedElement root)
    {
        final AtomicInteger remaining = new AtomicInteger(getChildren().size());
        final List<InitializedTemplate> init_children = new CopyOnWriteArrayList<InitializedTemplate>();
        final SettableFuture<InitializedTemplate> rs = SettableFuture.create();
        for (ProvisionedElement template : getChildren()) {
            final ListenableFuture<? extends InitializedTemplate> child = template.initialize(ec, ex, root);
            child.addListener(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        final InitializedTemplate ct = child.get();
                        init_children.add(ct);
                        if (remaining.decrementAndGet() == 0) {
                            rs.set(new InitializedSystem(getId(),
                                                         getType(),
                                                         getName(),
                                                         getMy(),
                                                         init_children));
                        }
                    }
                    catch (InterruptedException e) {
                        ec.error(e, "interrupted while initializing server");
                    }
                    catch (ExecutionException e) {
                        String msg = ec.error(e.getCause(), "Error while initializing server: %s", e.getCause()
                                                                                                    .getMessage());
                        logger.warn(e, msg);
                    }
                }
            }, ex);
        }
        return rs;
    }
}
