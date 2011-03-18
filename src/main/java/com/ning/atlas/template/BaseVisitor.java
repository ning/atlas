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

    public T visitServer(ServerTemplate node, int cardinality, T baton)
    {
        return baton;
    }
}
