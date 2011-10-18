package com.ning.atlas.spi;

import com.ning.atlas.NormalizedServerTemplate;
import com.ning.atlas.SystemMap;
import com.ning.atlas.Uri;

import java.util.concurrent.Future;

public interface Installer extends Component
{
    public void install(Server server,
                        String fragment,
                        Node root,
                        Node node) throws Exception;

    String describe(NormalizedServerTemplate server, Uri<Installer> uri, Space space);

    Future<?> install(NormalizedServerTemplate server, Uri<Installer> uri, Space space, SystemMap map);
}
