package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;

import java.util.List;

public class Bunch extends Element
{
    public Bunch(Identity id, My my, List<Element> children)
    {
        super(id, my, children);
    }
}
