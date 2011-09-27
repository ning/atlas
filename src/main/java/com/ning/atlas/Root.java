package com.ning.atlas;

import com.ning.atlas.spi.Node;

public class Root
{
    private final Node node;

    public Root(Node node) {
        this.node = node;
    }

    public Node getNode()
    {
        return node;
    }
}
