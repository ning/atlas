package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.base.Either;
import com.ning.atlas.base.MoreFutures;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.logging.Logger;
import com.ning.atlas.upgrade.UpgradePlan;
import com.ning.atlas.upgrade.UpgradeSystemPlan;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Iterables.addAll;

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

    @Override
    public List<? extends BoundTemplate> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    @Override
    public ListenableFuture<ProvisionedElement> provision(final ErrorCollector collector, final ExecutorService exec)
    {
        List<ListenableFuture<ProvisionedElement>> lof = Lists.newArrayListWithExpectedSize(getChildren().size());
        for (BoundTemplate template : getChildren()) {
            lof.add(template.provision(collector, exec));
        }

        ListenableFuture<List<Either<ProvisionedElement, ExecutionException>>> goop = MoreFutures.combine(lof);
        return Futures.chain(goop, new Function<List<Either<ProvisionedElement, ExecutionException>>, ListenableFuture<ProvisionedElement>>()
        {
            @Override
            public ListenableFuture<ProvisionedElement> apply(List<Either<ProvisionedElement, ExecutionException>> input)
            {
                List<ProvisionedElement> children = Lists.newArrayList();
                for (Either<ProvisionedElement, ExecutionException> either : input) {
                    switch (either.getSide()) {
                        case Success:
                            children.add(either.getSuccess());
                            break;
                        default:
                        case Failure:
                            Throwable cause = either.getFailure().getCause();
                            String msg = collector.error(cause, "Exception while processing a child: %s", cause.getMessage());
                            logger.warn(cause, msg);
                            break;
                    }
                }
                ProvisionedElement pe = new ProvisionedSystem(getId(), getType(), getName(), getMy(), children);
                return Futures.immediateFuture(pe);
            }
        });
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
