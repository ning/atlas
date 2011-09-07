package com.ning.atlas.tree;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class Trees
{
    public static <TreeType extends Tree, BatonType> BatonType visit(TreeType tree,
                                                                               BatonType baton,
                                                                               Visitor<TreeType, BatonType> visitor)
    {
        return new Director<TreeType, BatonType>(tree, visitor).apply(baton);
    }

    public static <TreeType extends Tree> List<TreeType> find(TreeType tree, final Predicate<TreeType> test)
    {
        return visit(tree, new ArrayList<TreeType>(), new BaseVisitor<TreeType, List<TreeType>>()
        {
            @Override
            public List<TreeType> on(TreeType node, List<TreeType> baton)
            {
                if (test.apply(node)) {
                    baton.add(node);
                }
                return baton;
            }
        });
    }

    public static <TreeType extends Tree> List<TreeType> leaves(TreeType root)
    {
        return visit(root, Lists.<TreeType>newArrayList(), new BaseVisitor<TreeType, List<TreeType>>()
        {

            public List<TreeType> on(TreeType node, List<TreeType> baton)
            {
                if (!node.getChildren().iterator().hasNext()) {
                    baton.add(node);
                }
                return baton;
            }
        });
    }

    public static <TreeType extends Tree, T extends TreeType> List<T> findInstancesOf(TreeType root, final Class<T> type)
    {
        return visit(root, new ArrayList<T>(), new BaseVisitor<TreeType, List<T>>()
        {
            @Override
            public List<T> on(TreeType node, List<T> baton)
            {
                if (type.isAssignableFrom(node.getClass())) {
                    baton.add(type.cast(node));
                }
                return baton;
            }
        });
    }

    private static class Director<TreeType extends Tree, BatonType>
    {
        private final TreeType                     tree;
        private final Visitor<TreeType, BatonType> visitor;

        Director(TreeType tree, Visitor<TreeType, BatonType> visitor)
        {
            this.tree = tree;
            this.visitor = visitor;
        }

        public BatonType apply(BatonType input)
        {
            BatonType b = visitor.enter(tree, input);
            b = visitor.on(tree, b);
            for (TreeType child : (Iterable<TreeType>) tree.getChildren()) {
                Director<TreeType, BatonType> d = new Director<TreeType, BatonType>(child, visitor);
                b = d.apply(b);
            }
            return visitor.exit(tree, b);
        }
    }
}
