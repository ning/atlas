package com.ning.atlas;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class ProvisionedErrorTemplate extends ProvisionedTemplate
{
    private final String message;

    public ProvisionedErrorTemplate(String name, String message)
    {
        super(name);
        this.message = message;
    }

    @Override
    public List<? extends ProvisionedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public ListenableFuture<? extends InitializedTemplate> initialize(Executor ex, ProvisionedTemplate root)
    {
        return Futures.immediateFuture(new InitializedErrorTemplate(getName(),
                                                                    "Unable to initialize server because " +
                                                                    "of previous provisioning error, '" +
                                                                    message + "'"));
    }

    @JsonProperty("error")
    public String getError() {
        return message;
    }
}
