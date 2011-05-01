package com.ning.atlas.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestMagicVisitor
{
    private Waffle root;

    @Before
    public void setUp() throws Exception
    {
        root = new Waffle(new Pancake(), new Waffle(new Pancake(), new Pancake()));
        /**
         * Waffle
         *   Pancake
         *   Waffle
         *     Pancake
         *     Pancake
         */
    }

    @Test
    public void testOnNoBaton() throws Exception
    {
        final AtomicInteger waffles = new AtomicInteger(0);
        final AtomicInteger pancakes = new AtomicInteger(0);
        Trees.visit(root, new ArrayList<Pancake>(), new MagicVisitor<Waffle, List<Pancake>>() {

            void on(Waffle waffle) {
                waffles.incrementAndGet();
            }

            void on(Pancake pancake) {
                pancakes.incrementAndGet();
            }
        });
        assertThat(waffles.get(), equalTo(2));
        assertThat(pancakes.get(), equalTo(3));
    }

    @Test
    public void testMagicVisitorSubclass() throws Exception
    {
        List<Pancake> rs = Trees.visit(root, new ArrayList<Pancake>(), new MagicVisitor<Waffle, List<Pancake>>()
        {
            public List<Pancake> enter(Pancake pancake, List<Pancake> baton)
            {
                baton.add(pancake);
                return baton;
            }
        });
        assertThat(rs.size(), equalTo(3));
    }

    @Test
    public void testMagicVisitorDelegate() throws Exception
    {
        List<Pancake> rs = Trees.visit(root, new ArrayList<Pancake>(), new MagicVisitor<Waffle, List<Pancake>>(new Object()
        {
            public List<Pancake> enter(Pancake pancake, List<Pancake> baton)
            {
                baton.add(pancake);
                return baton;
            }
        }));
        assertThat(rs.size(), equalTo(3));
    }
}
