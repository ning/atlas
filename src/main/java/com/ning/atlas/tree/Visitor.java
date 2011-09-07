package com.ning.atlas.tree;

public interface Visitor<TreeType extends Tree, BatonType>
{
    BatonType enter(TreeType node, BatonType baton);

    BatonType on(TreeType node, BatonType baton);

    BatonType exit(TreeType node, BatonType baton);
}
