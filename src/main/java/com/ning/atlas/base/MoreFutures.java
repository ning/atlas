package com.ning.atlas.base;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class MoreFutures
{
    public static <T> ListenableFuture<List<Either<T, ExecutionException>>> invertify(List<ListenableFuture<T>> list_of_futures)
    {
        final SettableFuture<List<Either<T, ExecutionException>>> rs = SettableFuture.create();
        int idx = 0;
        final int expected_size = list_of_futures.size();
        final CopyOnWriteArrayList<Pair<Integer, Either<T, ExecutionException>>> ls = new CopyOnWriteArrayList<Pair<Integer, Either<T, ExecutionException>>>();
        for (final ListenableFuture<T> future : list_of_futures) {
            final int l_idx = idx++;
            future.addListener(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        T t = future.get();
                        ls.add(Pair.<Integer, Either<T, ExecutionException>>of(l_idx, Either.<T, ExecutionException>success(t)));
                    }
                    catch (InterruptedException e) {
                        rs.setException(e);
                    }
                    catch (ExecutionException e) {
                        ls.add(Pair.<Integer, Either<T, ExecutionException>>of(l_idx, Either.<T, ExecutionException>failure(e)));
                    }
                    finally {
                        if (ls.size() == expected_size) {
                            ArrayList<Pair<Integer, Either<T, ExecutionException>>> local = Lists.newArrayList(ls);

                            Collections.sort(local, new Comparator<Pair<Integer, Either<T, ExecutionException>>>()
                            {
                                @Override
                                public int compare(Pair<Integer, Either<T, ExecutionException>> o1, Pair<Integer, Either<T, ExecutionException>> o2)
                                {
                                    return o1.getKey().compareTo(o2.getKey());
                                }
                            });
                            rs.set(Lists.transform(local, new Function<Pair<Integer, Either<T, ExecutionException>>, Either<T, ExecutionException>>()
                            {
                                @Override
                                public Either<T, ExecutionException> apply(@Nullable Pair<Integer, Either<T, ExecutionException>> input)
                                {
                                    return input == null ? null : input.getRight();
                                }
                            }));
                        }
                    }
                }
            }, MoreExecutors.sameThreadExecutor());
        }
        return rs;
    }
}
