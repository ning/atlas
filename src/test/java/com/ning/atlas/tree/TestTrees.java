package com.ning.atlas.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestTrees
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

    @Test
    public void testLeaves() throws Exception
    {
        List<Waffle> leaves = Trees.leaves(root);
        assertThat(leaves.size(), equalTo(3));
    }

    @Test
    public void testVisit() throws Exception
    {
        List<String> types = Trees.visit(root, new ArrayList<String>(), new BaseVisitor<Waffle, List<String>>()
        {

            public List<String> enter(Waffle node, List<String> baton)
            {
                baton.add("+" + node.getClass().getSimpleName());
                return baton;
            }

            public List<String> exit(Waffle node, List<String> baton)
            {
                baton.add("-" + node.getClass().getSimpleName());
                return baton;
            }
        });
        assertThat(types, equalTo(Arrays.asList("+Waffle",
                                                "+Pancake",
                                                "-Pancake",
                                                "+Waffle",
                                                "+Pancake",
                                                "-Pancake",
                                                "+Pancake",
                                                "-Pancake",
                                                "-Waffle",
                                                "-Waffle")));
    }


}
