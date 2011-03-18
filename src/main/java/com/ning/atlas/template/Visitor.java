package com.ning.atlas.template;

public interface Visitor<T>
{
    /**
     * Called on way down the tree
     */
    T enterSystem(SystemTemplate node, int cardinality, T baton);

    /**
     * Called on way back up the tree
     */
    T leaveSystem(SystemTemplate node, int cardinality, T baton);


    /**
     * Called on leaf services
     */
    T visitServer(ServerTemplate node, int cardinality, T baton);
}
