package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.base.Either;
import com.ning.atlas.base.MoreFutures;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ProvisionedSystem extends ProvisionedElement
{
    private final static Logger logger = Logger.get(ProvisionedSystem.class);

    private final List<ProvisionedElement> children;

    public ProvisionedSystem(Identity id, String type, String name, My my, List<ProvisionedElement> children)
    {
        super(id, type, name, my);
        this.children = new ArrayList<ProvisionedElement>(children);
    }

    public List<ProvisionedElement> getChildren()
    {
        return children;
    }

    @Override
    protected ListenableFuture<InitializedTemplate> initialize(final ErrorCollector ec, final Executor ex, final ProvisionedElement root)
    {
        final List<ListenableFuture<InitializedTemplate>> lof = Lists.newArrayListWithExpectedSize(getChildren().size());
        for (ProvisionedElement element : getChildren()) {
            lof.add(element.initialize(ec, ex, root));
        }

        ListenableFuture<List<Either<InitializedTemplate, ExecutionException>>> goop = MoreFutures.invertify(lof);

        return Futures.chain(goop, new Function<List<Either<InitializedTemplate, ExecutionException>>, ListenableFuture<? extends InitializedTemplate>>()
        {
            @Override
            public ListenableFuture<? extends InitializedTemplate> apply(List<Either<InitializedTemplate, ExecutionException>> input)
            {
                List<InitializedTemplate> children = Lists.newArrayList();
                for (Either<InitializedTemplate, ExecutionException> either : input) {
                    switch (either.getSide()) {
                        case Success:
                            children.add(either.getSuccess());
                            break;
                        default:
                        case Failure:
                            Throwable cause = either.getFailure().getCause();
                            String msg = ec.error(cause, "Failure while initializing child: %s", cause.getMessage());
                            logger.warn(cause, msg);
                            break;
                    }
                }
                InitializedTemplate t = new InitializedSystem(getId(), getType(), getName(), getMy(), children);
                return Futures.immediateFuture(t);
            }
        });
    }
}
