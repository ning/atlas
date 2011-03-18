package com.ning.atlas.template;

public abstract class UnifiedVisitor<T> implements Visitor<T>
{
    public final T enterSystem(SystemTemplate node, int cardinality, T baton)
    {
        return enterNode(node, cardinality, baton);
    }

    public final T leaveSystem(SystemTemplate node, int cardinality, T baton)
    {
        return leaveNode(node, cardinality, baton);
    }

    public final T visitService(ServiceTemplate node, int cardinality, T baton)
    {
        return leaveNode(node, cardinality, enterNode(node, cardinality, baton));
    }


    public abstract T enterNode(DeployTemplate node, int cardinality, T baton);

    public abstract T leaveNode(DeployTemplate node, int cardinality, T baton);
}
