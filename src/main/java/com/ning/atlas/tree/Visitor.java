package com.ning.atlas.tree;

public interface Visitor<TreeType extends Tree<TreeType>, BatonType>
{
    BatonType enter(TreeType node, BatonType baton);
    BatonType on(TreeType node, BatonType baton);
    BatonType exit(TreeType node, BatonType baton);
}
