package com.ning.atlas.tree;

public abstract class BaseVisitor<TreeType extends Tree<TreeType>, BatonType> implements Visitor<TreeType, BatonType>
{
    public BatonType enter(TreeType node, BatonType baton)
    {
        return baton;
    }

    public BatonType on(TreeType node, BatonType baton)
    {
        return baton;
    }

    public BatonType exit(TreeType node, BatonType baton)
    {
        return baton;
    }
}
