package com.ning.atlas.space;

import com.google.common.collect.ImmutableSet;
import com.ning.atlas.spi.Identity;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestSpaceQuery
{

    private InMemorySpace space;

    @Before
    public void setUp() throws Exception
    {
        space = InMemorySpace.newInstance();
        space.store(Identity.valueOf("/root.0/thing.0"), "name", "Brian");
        space.store(Identity.valueOf("/root.0/thing.1"), "name", "Sam");
        space.store(Identity.valueOf("/root.0/thing.2"), "name", "Kate");

        space.store(Identity.valueOf("/root.1/bot.0"), "name", "Google");
        space.store(Identity.valueOf("/root.1/bot.1"), "name", "Bing");

        space.store(Identity.valueOf("/root.1/bot.0/hello.world"), "name", "Altavista");
    }

    @Test
    public void testFoo() throws Exception
    {
        SpaceQuery sq = new QueryParser().parse("/*/thing.*:name");
        Set<String> rs = sq.query(space);

        Set<String> expected = ImmutableSet.of("Brian", "Sam", "Kate");
        assertThat(rs, equalTo(expected));
    }

    @Test
    public void testBar() throws Exception
    {
        SpaceQuery sq = new QueryParser().parse("/root.1/*.1:name");
        Set<String> rs = sq.query(space);

        Set<String> expected = ImmutableSet.of("Bing");
        assertThat(rs, equalTo(expected));
    }

    @Test
    public void testBaz() throws Exception
    {
        SpaceQuery sq = new QueryParser().parse("/root.1/*/*:name");
        Set<String> rs = sq.query(space);

        Set<String> expected = ImmutableSet.of("Altavista");
        assertThat(rs, equalTo(expected));
    }

    @Test
    public void testJohann() throws Exception
    {
        SpaceQuery sq = new QueryParser().parse("/*/<t\\w+>.*:name");
        Set<String> rs = sq.query(space);

        Set<String> expected = ImmutableSet.of("Brian", "Sam", "Kate");
        assertThat(rs, equalTo(expected));
    }

}
