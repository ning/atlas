package com.ning.atlas.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestFutureStuff
{

    private ListenableExecutorService exec;

    @Before
    public void setUp() throws Exception
    {
        this.exec = ListenableExecutorService.delegateTo(Executors.newCachedThreadPool());
    }

    @After
    public void tearDown() throws Exception
    {
        this.exec.shutdown();
    }

    @Test
    public void testFoo() throws Exception
    {
        ListenableFuture<String> f1 = exec.submit(new Callable<String>() {
            public String call() throws Exception
            {
                return "hello";
            }
        });

        ListenableFuture<String> f2 = exec.submit(new Callable<String>() {
            public String call() throws Exception
            {
                return "world";
            }
        });

        List<ListenableFuture<String>> list_of_futures = asList(f1, f2);

        ListenableFuture<List<Either<String, ExecutionException>>> rs = MoreFutures.invertify(list_of_futures);
        List<Either<String, ExecutionException>> ls = rs.get();
        assertThat(ls.size(), equalTo(2));

        assertThat(ls.get(0).getSide(), equalTo(Either.Side.Success));
        assertThat(ls.get(0).getSuccess(), equalTo("hello"));

        assertThat(ls.get(1).getSide(), equalTo(Either.Side.Success));
        assertThat(ls.get(1).getSuccess(), equalTo("world"));
    }

    @Test
    public void testHatingLife() throws Exception
    {
        ListenableFuture<Map<String, String>> f1 = exec.submit(new Callable<Map<String, String>>() {
            public Map<String, String> call() throws Exception
            {
                return ImmutableMap.of("hello", "world");
            }
        });

        ListenableFuture<Map<String, String>> f2 = exec.submit(new Callable<Map<String, String>>() {
            public Map<String, String> call() throws Exception
            {
                return ImmutableMap.of("hello", "fred");
            }
        });

        List<ListenableFuture<Map<String, String>>> list_of_futures = asList(f1, f2);

        ListenableFuture<List<Either<Map<String, String>, ExecutionException>>> rs = MoreFutures.invertify(list_of_futures);
        List<Either<Map<String, String>, ExecutionException>> ls = rs.get();
        assertThat(ls.size(), equalTo(2));

        assertThat(ls.get(0).getSide(), equalTo(Either.Side.Success));
        assertThat(ls.get(0).getSuccess(), equalTo((Map<String, String>)ImmutableMap.of("hello", "world")));

        assertThat(ls.get(1).getSide(), equalTo(Either.Side.Success));
        assertThat(ls.get(1).getSuccess(), equalTo((Map<String, String>)ImmutableMap.of("hello", "fred")));
    }


}
