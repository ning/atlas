package com.ning.atlas;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.Futures;
import com.ning.atlas.spi.BaseComponent;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Space;
import com.ning.atlas.spi.Status;
import com.ning.atlas.spi.Uri;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

public class ScratchInstaller extends BaseComponent implements Installer
{
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Future<Status> install(Host server, Uri<Installer> uri, Deployment deployment)
    {
        final String id = server.getId().toExternalForm();
        final Space space = deployment.getSpace();
        Map<String, String> pairs = Splitter.on(";").trimResults().withKeyValueSeparator("=").split(uri.getFragment());
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            space.scratch(key.replaceAll("@", id), value.replaceAll("@", id));
        }

        return Futures.immediateFuture(Status.okay("wrote out value"));

    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return null;
    }
}
