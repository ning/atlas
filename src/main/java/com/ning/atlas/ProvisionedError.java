package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class ProvisionedError extends ProvisionedElement
{
    private final String message;

    public ProvisionedError(Identity id, String type, String name, My my, String message)
    {
        super(id, type, name, my);
        this.message = message;
    }

    @Override
    public List<? extends ProvisionedElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<? extends InitializedTemplate> initialize(final ErrorCollector ec, Executor ex, ProvisionedElement root)
    {
        return Futures.immediateFuture(new InitializedError(getId(), getType(), getType(), getMy(),
                                                            "Unable to initialize server because " +
                                                            "of previous provisioning error, '" +
                                                            message + "'"));
    }

    @JsonProperty("error")
    public String getError()
    {
        return message;
    }
}
