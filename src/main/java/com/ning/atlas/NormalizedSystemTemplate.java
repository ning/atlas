package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;

import java.util.List;

public class NormalizedSystemTemplate extends NormalizedTemplate
{
    public NormalizedSystemTemplate(Identity id, My my, List<NormalizedTemplate> children)
    {
        super(id, my, children);
    }
}
