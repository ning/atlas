package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.errors.ErrorCollector;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
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

    public ProvisionedError(Throwable cause, String msg)
    {
        super(Identity.root().createChild("unknown",
                                          String.valueOf(System.identityHashCode(cause))),
              "unknown",
              String.valueOf(System.identityHashCode(cause)),
              new My());
        this.message = msg;
    }

    @Override
    public List<ProvisionedElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    protected ListenableFuture<InitializedTemplate> initialize(final ErrorCollector ec, Executor ex, ProvisionedElement root)
    {

        return Futures.immediateFuture((InitializedTemplate) new InitializedError(getId(), getType(), getType(), getMy(),
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
