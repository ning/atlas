package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class TestBucketingMuckery
{

    /**
     * @param sources list of list of T
     * @param groupBy extracts the thing to group by
     *
     * @return list of sets grouped by keyGetter, preserving order as you traverse list
     */
    public static <T, K extends Comparable<K>> List<Stage<T>> schedule(List<List<T>> sources, Function<T, K> groupBy)
    {
        /*
        Stage contains set of Groups.
         */
        List<Stage<T>> stages = Lists.newArrayList();

        List<Iterator<T>> iterators = Lists.newArrayListWithExpectedSize(sources.size());
        for (Iterable<T> source : sources) {
            iterators.add(source.iterator());
        }

        Set<T> groups_in_current_stage = Sets.newLinkedHashSet();
        for (Iterator<T> iterator : iterators) {

        }


        return stages;
    }


    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        List<Thing> one = list("1a 1b 1c 1f");
        List<Thing> two = list("2b");
        List<Thing> three = list("3a 3c 3d");

        List<Stage<Thing>> buckets = schedule(asList(one, two, three), new ExtractStringFromThing());
        assertThat(buckets, equalTo(parse("[{1a 3a}] [{1b 2b}] [{1c 3c}] [{3d} {1f}]")));
    }

    // ************ helper stuff ****************

    @Test
    public void testParse() throws Exception
    {
        List<Stage<Thing>> stages = parse("[{1a 3a}] [{1b 2b}] [{1c 3c}] [{3d} {1f}]");

        Stage<Thing> one = new Stage<Thing>(setOf(new Group<Thing>(setOf(t(1, "a"), t(3, "a")))));
        Stage<Thing> two = new Stage<Thing>(setOf(new Group<Thing>(setOf(t(1, "b"), t(2, "b")))));
        Stage<Thing> thr = new Stage<Thing>(setOf(new Group<Thing>(setOf(t(1, "c"), t(3, "c")))));
        Stage<Thing> fou = new Stage<Thing>(setOf(new Group<Thing>(setOf(t(3, "d"))), new Group<Thing>(setOf(t(1, "f")))));

        assertThat(stages, equalTo(asList(one, two, thr, fou)));
    }

    @Test
    public void testList() throws Exception
    {
        List<Thing> ts = list("1a 1b 1d");
        assertThat(ts, equalTo(asList(t(1, "a"), t(1, "b"), t(1, "d"))));
    }


    public static <T> Set<T> setOf(T... ts)
    {
        return ImmutableSet.copyOf(ts);
    }

    public static List<Thing> list(String descr)
    {
        Iterable<String> parts = Splitter.on(Pattern.compile("\\s+")).split(descr);
        List<Thing> rs = Lists.newArrayList();
        for (String s : parts) {
            Matcher m2 = Pattern.compile("(\\d+)([a-zA-Z]+)").matcher(s);
            m2.matches();
            rs.add(t(Integer.parseInt(m2.group(1)), m2.group(2)));
        }
        return rs;
    }

    // [{1a 3a}] [{1b 2b}] [{1c 3c}] [{3d} {1f}]
    // [] == stage
    // {} == group
    public static List<Stage<Thing>> parse(String desc)
    {
        List<Stage<Thing>> stages = Lists.newArrayList();
        Matcher stage_matcher = Pattern.compile("\\[\\s*(.+?)\\s*\\]").matcher(desc);
        while (stage_matcher.find()) {
            String contents = stage_matcher.group(1);

            Matcher group_matcher = Pattern.compile("\\{\\s*(.+?)\\s*\\}").matcher(contents);
            List<Set<Thing>> rs = Lists.newArrayList();
            while (group_matcher.find()) {
                String group_content = group_matcher.group(1);
                Iterable<String> itty = Splitter.on(Pattern.compile("[,\\s]+")).split(group_content);
                Set<Thing> group = Sets.newLinkedHashSet();
                for (String t : itty) {
                    Matcher m2 = Pattern.compile("(\\d+)([a-zA-Z]+)").matcher(t);
                    m2.matches();
                    group.add(t(Integer.parseInt(m2.group(1)), m2.group(2)));
                }
                rs.add(group);
            }

            Set<Group<Thing>> groups = Sets.newLinkedHashSet();
            for (Set<Thing> things : rs) {
                Group<Thing> g = new Group<Thing>(things);
                groups.add(g);
            }
            stages.add(new Stage<Thing>(groups));
        }

        return stages;
    }


    public static class Thing
    {
        private final int    i;
        private final String s;

        Thing(int i, String s)
        {
            this.i = i;
            this.s = s;
        }

        public int getInt()
        {
            return i;
        }

        public String getString()
        {
            return s;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }

        @Override
        public boolean equals(Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    public static Thing t(int i, String s)
    {
        return new Thing(i, s);
    }

    public static class ExtractStringFromThing implements Function<Thing, String>
    {
        @Override
        public String apply(Thing input)
        {
            return input.getString();
        }
    }

    public static class Group<T>
    {
        private final Set<T> elements;

        public Group(Set<T> elements)
        {
            this.elements = elements;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }

        @Override
        public boolean equals(Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    public static class Stage<T>
    {
        private final Set<Group<T>> groups;

        Stage(Set<Group<T>> groups)
        {
            this.groups = groups;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }

        @Override
        public boolean equals(Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }


}
