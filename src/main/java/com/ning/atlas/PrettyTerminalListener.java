package com.ning.atlas;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.BaseLifecycleListener;
import com.ning.atlas.spi.Deployment;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Installer;
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
    public Future<?> startDeployment(Deployment d)
    {
        d.getEventBus().subscribe(NotificationBus.Scope.Deployment, this);

        Set<Host> hosts = d.getSystemMap().findLeaves();

        int width = 0;
        for (Host host : hosts) {
            if (host.getId().toExternalForm().length() > width) {
                width = host.getId().toExternalForm().length();
            }
            System.out.println();
        }

        int offset = 0;
        for (Host host : hosts) {
            gauges.put(host.getId(), new Gauge(d, host, width, offset++));
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
    public void on(StartServerProvision evt)
    {
        gauges.get(evt.getServerId()).startProvision();
    }

    @Subscribe
    public void on(FinishedServerProvision evt)
    {
        gauges.get(evt.getServerId()).finishProvision();
    }

    @Subscribe
    public void on(FinishedServerInstall evt)
    {
        gauges.get(evt.getServerId()).installed(evt.getInstall());
    }


    private static class Gauge
    {
        private final ProgressBar progress;
        private final Set<Uri<Installer>> installs = Collections.synchronizedSet(Sets.<Uri<Installer>>newHashSet());
        private final double numberOfInstalls;
        private volatile double pct = 0D;
        private final Identity id;

        Gauge(Deployment d, Host host, int width, int offset)
        {
            this.id = host.getId();
            this.progress = new ProgressBar(Label.create(host.getId().toExternalForm(), width),
                                            Height.fromBottom(offset),
                                            Percentage.show());

            for (Uri<Installer> uri : d.getEnvironment().findBase(host.getBase()).getValue().getInitializations()) {
                installs.add(uri);
            }

            for (Uri<Installer> uri : host.getInstallations()) {
                installs.add(uri);
            }

            numberOfInstalls = installs.size();
            progress.render();
        }

        synchronized void startProvision()
        {
            pct += 0.01;
            progress.progress(pct).render();
        }

        synchronized void finishProvision()
        {
            pct += 0.09;
            progress.progress(pct).render();
        }

        synchronized void installed(Uri<Installer> install)
        {
            installs.remove(install);

            pct += 0.9 * ( (numberOfInstalls - installs.size()) / numberOfInstalls);

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
