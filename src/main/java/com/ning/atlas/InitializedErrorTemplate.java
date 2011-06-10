package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

public class InitializedErrorTemplate extends InitializedTemplate
{
    private final String message;

    public InitializedErrorTemplate(String name, String message)
    {
        super(name);
        this.message = message;
    }

    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @JsonProperty("error")
    public String getError() {
        return message;
    }
}
