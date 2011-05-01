package com.ning.atlas.template;

public class BaseVisitor<T> implements Visitor<T>
{
    public T enterSystem(ConfigurableSystemTemplate node, int cardinality, T baton)
    {
        return baton;
    }

    public T leaveSystem(ConfigurableSystemTemplate node, int cardinality, T baton)
    {
        return baton;
    }

    public T visitServer(ConfigurableServerTemplate node, int cardinality, T baton)
    {
        return baton;
    }
}
