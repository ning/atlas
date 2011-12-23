package com.ning.atlas;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
import com.ning.atlas.spi.Provisioner;
import com.ning.atlas.spi.Uri;
import com.ning.atlas.spi.bus.FinishedServerInstall;
import com.ning.atlas.spi.bus.FinishedServerProvision;
import com.ning.atlas.spi.bus.NotificationBus;
import com.ning.atlas.spi.bus.StartServerProvision;
import com.ning.atlas.spi.bus.Subscribe;
import org.skife.terminal.Height;
import org.skife.terminal.Label;
import org.skife.terminal.Percentage;
import org.skife.terminal.ProgressBar;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class PrettyTerminalListener extends BaseLifecycleListener
{
    private final Map<Identity, Gauge> gauges = Maps.newConcurrentMap();

    @Override
    public Future<?> startProvision(Deployment d)
    {
        d.getEventBus().subscribe(NotificationBus.Scope.Deployment, this);

        Set<Host> hosts = d.getSystemMap().findLeaves();

        int width = 0;
        for (Host host : hosts) {
            if (host.getId().toExternalForm().length() > width) {
                width = host.getId().toExternalForm().length();
            }
        }

        int offset = 0;
        for (Host host : hosts) {
            gauges.put(host.getId(), new Gauge(host, width, offset++));
        }

        return super.startDeployment(d);
    }

    @Override
    public Future<?> finishDeployment(Deployment d)
    {
        ProgressBar.moveCursorToBottomRight();
        return super.finishDeployment(d);
    }

    @Subscribe
    public void on(FinishedServerProvision evt)
    {
        gauges.get(evt.getServerId()).installed(evt.getUri());
    }

    @Subscribe
    public void on(FinishedServerInstall evt)
    {
        gauges.get(evt.getServerId()).installed(evt.getInstall());
    }


    private static class Gauge
    {
        private final ProgressBar progress;
        private final Set<Uri<?>> things = Collections.synchronizedSet(Sets.<Uri<?>>newHashSet());
        private final double numberOfInstalls;

        Gauge(Host host, int width, int offset)
        {
            this.progress = new ProgressBar(Label.create(host.getId().toExternalForm(), width),
                                            Height.fromBottom(offset),
                                            Percentage.show());

            for (Uri<Installer> uri : host.getInitializationUris()) {
                things.add(uri);
            }

            for (Uri<Installer> uri : host.getInstallationUris()) {
                things.add(uri);
            }

            numberOfInstalls = things.size();
        }

        synchronized void installed(Uri<?> install)
        {
            things.remove(install);

            double pct = ( (numberOfInstalls - things.size()) / numberOfInstalls);

            if (pct >= 0.97) {
                pct = 1.0;
            }

            try {
                progress.progress(pct).render().get();
            }
            catch (Exception e) {
                // NOOP
            }
        }
    }
}
