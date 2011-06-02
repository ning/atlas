package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProvisionedServerTemplate extends ProvisionedTemplate
{
    private final String externalIpAddress;
    private final String internalIpAddress;

    @JsonIgnore
    private final Server server;


    public ProvisionedServerTemplate(String name, Server server) {
        super(name);
        this.server = server;
        this.externalIpAddress = server.getExternalIpAddress();
        this.internalIpAddress = server.getInternalIpAddress();
    }

    public ProvisionedServerTemplate(BoundServerTemplate boundServerTemplate, Server server)
    {
        this(boundServerTemplate.getName(), server);
    }

    @JsonIgnore
    public List<? extends ProvisionedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<InitializedTemplate> initialize()
    {
        final ListenableFuture<? extends Server> initialized_server = server.initialize();
        final SettableFuture<InitializedTemplate> rs = SettableFuture.create();
        initialized_server.addListener(new Runnable() {
                                           @Override
                                           public void run()
                                           {
                                               try {
                                                   final Server s = initialized_server.get();
                                                   rs.set(new InitializedServerTemplate(getName(), s));
                                               }
                                               catch (InterruptedException e) {
                                                   rs.setException(e);
                                               }
                                               catch (ExecutionException e) {
                                                   rs.setException(e.getCause());
                                               }
                                           }
                                       }, MoreExecutors.sameThreadExecutor());
        return rs;
    }

    public String getExternalIP() {
        return externalIpAddress;
    }

    public String getInternalIP() {
        return internalIpAddress;
    }
}
