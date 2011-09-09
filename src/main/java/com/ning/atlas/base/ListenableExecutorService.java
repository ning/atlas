package com.ning.atlas.base;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ListenableExecutorService extends AbstractExecutorService
{
    private final ExecutorService delegate;

    private ListenableExecutorService(ExecutorService delegate)
    {
        this.delegate = delegate;
    }

    public static ListenableExecutorService delegateTo(ExecutorService delegate)
    {
        return new ListenableExecutorService(delegate);
    }

    @Override
    public void shutdown()
    {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown()
    {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command)
    {
        delegate.execute(command);
    }

    protected <T> ListenableFutureTask<T> newTaskFor(Runnable runnable, @Nullable T value)
    {
        return new ListenableFutureTask<T>(runnable, value);
    }

    protected <T> ListenableFutureTask<T> newTaskFor(Callable<T> callable)
    {
        return new ListenableFutureTask<T>(callable);
    }


    /* Following overridden to return covariant type*/

    @Override
    public ListenableFuture<?> submit(Runnable task)
    {
        return (ListenableFuture<?>) super.submit(task);
    }

    @Override
    public <T> ListenableFuture<T> submit(Runnable task, T result)
    {
        return (ListenableFuture<T>) super.submit(task, result);
    }

    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task)
    {
        return (ListenableFuture<T>) super.submit(task);
    }

    public <T> List<ListenableFuture<T>> invokeAllListenable(Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        return Lists.transform(super.invokeAll(tasks), new Function<Future<T>, ListenableFuture<T>>()
        {
            @Override
            public ListenableFuture<T> apply(@Nullable Future<T> input)
            {
                return (ListenableFuture<T>) input;
            }
        });
    }

    public <T> List<ListenableFuture<T>> invokeAllListenable(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
    {
        return Lists.transform(super.invokeAll(tasks, timeout, unit), new Function<Future<T>, ListenableFuture<T>>()
        {
            @Override
            public ListenableFuture<T> apply(@Nullable Future<T> input)
            {
                return (ListenableFuture<T>)input;
            }
        });
    }
}

