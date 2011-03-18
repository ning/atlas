package com.ning.atlas.template;

public class BaseVisitor<T> implements Visitor<T>
{
    public T enterSystem(SystemTemplate node, int cardinality, T baton)
    {
        return baton;
    }

    public T leaveSystem(SystemTemplate node, int cardinality, T baton)
    {
        return baton;
    }

    public T visitService(ServiceTemplate node, int cardinality, T baton)
    {
        return baton;
    }
}
