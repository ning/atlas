package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Collections;
import java.util.List;

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
    public ListenableFuture<? extends InitializedTemplate> initialize()
    {
        Server initialized_server = server.initialize();
        return Futures.immediateFuture(new InitializedServerTemplate(getName(), initialized_server));
    }

    public String getExternalIP() {
        return externalIpAddress;
    }

    public String getInternalIP() {
        return internalIpAddress;
    }
}
