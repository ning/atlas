package com.ning.atlas.packages;

import com.ning.atlas.ConcurrentComponent;
import com.ning.atlas.Host;
import com.ning.atlas.spi.Component;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Uri;

import java.util.concurrent.Future;

public class AptInstaller extends ConcurrentComponent<String>
{



    @Override
    public String perform(Host host, Uri<? extends Component> uri, Deployment d) throws Exception
    {
        return null;
    }

    @Override
    public Future<String> describe(Host server, Uri<? extends Component> uri, Deployment deployment)
    {
        return null;
    }
}
