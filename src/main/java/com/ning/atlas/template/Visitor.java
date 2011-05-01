package com.ning.atlas.template;

public interface Visitor<T>
{
    /**
     * Called on way down the tree
     */
    T enterSystem(ConfigurableSystemTemplate node, int cardinality, T baton);

    /**
     * Called on way back up the tree
     */
    T leaveSystem(ConfigurableSystemTemplate node, int cardinality, T baton);


    /**
     * Called on leaf services
     */
    T visitServer(ConfigurableServerTemplate node, int cardinality, T baton);
}
