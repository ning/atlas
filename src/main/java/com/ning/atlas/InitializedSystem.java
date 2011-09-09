package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.atlas.base.Either;
import com.ning.atlas.base.MoreFutures;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
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
    protected ListenableFuture<InstalledElement> install(final ErrorCollector ec, ExecutorService ex, InitializedTemplate root)
    {
        List<ListenableFuture<InstalledElement>> lof = Lists.newArrayListWithExpectedSize(getChildren().size());
        for (InitializedTemplate template : getChildren()) {
            lof.add(template.install(ec, ex));
        }

        ListenableFuture<List<Either<InstalledElement, ExecutionException>>> goop = MoreFutures.combine(lof);

        return Futures.chain(goop, new Function<List<Either<InstalledElement, ExecutionException>>, ListenableFuture<? extends InstalledElement>>()
        {
            @Override
            public ListenableFuture<? extends InstalledElement> apply(List<Either<InstalledElement, ExecutionException>> input)
            {
                List<InstalledElement> children = Lists.newArrayList();
                for (Either<InstalledElement, ExecutionException> either : input) {
                    switch (either.getSide()) {
                        case Success:
                            children.add(either.getSuccess());
                            break;
                        default:
                        case Failure:
                            Throwable cause = either.getFailure().getCause();
                            String msg = ec.error(cause, "Exception installing a child: %s", cause.getMessage());
                            log.warn(cause, msg);
                            break;
                    }
                }
                return Futures.immediateFuture(new InstalledSystem(getId(), getType(), getName(), getMy(), children));
            }
        });
    }
}
