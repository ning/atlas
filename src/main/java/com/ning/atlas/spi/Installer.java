package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.Space;
import com.ning.atlas.Uri;

public interface Installer
{
    public void install(Server server,
                        String fragment,
                        Node root,
                        Node node) throws Exception;

    String describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space);
}
