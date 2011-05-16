package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ning.atlas.tree.Trees;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.ning.atlas.tree.Trees.leaves;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestTemplateBuilding
{

    private SystemTemplate root;
    private Environment    env;

    @Before
    public void setUp() throws Exception
    {
        root = new SystemTemplate("root");
        root.addChildren(Arrays.asList(new ServerTemplate("happy") {{ setCardinality(3); setBase("waffle"); }},
                                       new ServerTemplate("sad") {{ setCardinality(2); setBase("pancake"); }}));
        env = new Environment("desktop");
        env.addBase(new Base("waffle", env, Collections.<String, String>emptyMap()));
        env.addBase(new Base("pancake", env, Collections.<String, String>emptyMap()));
    }

    @Test
    public void testCardinlaityNormalization() throws Exception
    {
        List<BoundTemplate> normalized_root = newArrayList(root.normalize(env));
        assertThat(normalized_root.size(), equalTo(1));

        List<BoundTemplate> leaves = Trees.leaves(normalized_root.get(0));
        List<BoundTemplate> happy = Lists.newArrayList(Iterables.filter(leaves, new Predicate<BoundTemplate>()
        {
            public boolean apply(@Nullable BoundTemplate input)
            {
                return "happy".equals(input.getName());
            }
        }));

        List<BoundTemplate> sad = Lists.newArrayList(Iterables.filter(leaves, new Predicate<BoundTemplate>()
        {
            public boolean apply(@Nullable BoundTemplate input)
            {
                return "sad".equals(input.getName());
            }
        }));

        assertThat(happy.size(), equalTo(3));
        assertThat(sad.size(), equalTo(2));
    }

    @Test
    public void testConvertBase() throws Exception
    {
        env = new Environment("desktop");

        Base w = new Base("waffle", env);
        w.getAttributes().put("ami", "ami-1234");
        env.addBase(w);

        Base p = new Base("pancake", env);
        p.getAttributes().put("ami", "ami-6789");
        env.addBase(p);

        final List<BoundTemplate> normalized_root = newArrayList(root.normalize(env));

        Function<String, Base> lookup_base_for_first_NAME_in_normalized = new Function<String, Base>()
        {
            public Base apply(@Nullable final String input)
            {
                BoundTemplate happy = find(leaves(normalized_root.get(0)), new Predicate<BoundTemplate>()
                {
                    public boolean apply(@Nullable BoundTemplate input2)
                    {
                        return input.equals(input2.getName());
                    }
                });

                assertThat(happy, instanceOf(BoundServerTemplate.class));
                BoundServerTemplate ht = (BoundServerTemplate) happy;
                return ht.getBase();
            }
        };

        assertThat(lookup_base_for_first_NAME_in_normalized.apply("happy"), is(w));
        assertThat(lookup_base_for_first_NAME_in_normalized.apply("sad"), is(p));
    }

    @Test
    public void testServerCardinalityOverride() throws Exception
    {
        env.override("root.happy:cardinality", "5");
        List<BoundTemplate> normalized_root = newArrayList(root.normalize(env));
        assertThat(normalized_root.size(), equalTo(1));

        List<BoundTemplate> leaves = Trees.leaves(normalized_root.get(0));
        List<BoundTemplate> happy = Lists.newArrayList(Iterables.filter(leaves, new Predicate<BoundTemplate>()
        {
            public boolean apply(@Nullable BoundTemplate input)
            {
                return "happy".equals(input.getName());
            }
        }));

        List<BoundTemplate> sad = Lists.newArrayList(Iterables.filter(leaves, new Predicate<BoundTemplate>()
        {
            public boolean apply(@Nullable BoundTemplate input)
            {
                return "sad".equals(input.getName());
            }
        }));

        assertThat(happy.size(), equalTo(5));
        assertThat(sad.size(), equalTo(2));

    }
    @Test
    public void testSystemCardinalityOverride() throws Exception
    {
        List<BoundTemplate> normalized_root = newArrayList(root.normalize(env));
        assertThat(normalized_root.size(), equalTo(1));
    }
}
