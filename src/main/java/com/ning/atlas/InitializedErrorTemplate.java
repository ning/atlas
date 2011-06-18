package com.ning.atlas;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

public class InitializedErrorTemplate extends InitializedTemplate
{
    private final String message;

    public InitializedErrorTemplate(String type, String name, My my, String message)
    {
        super(type, name, my);
        this.message = message;
    }

    @Override
    public List<? extends InitializedTemplate> getChildren()
    {
        return Collections.emptyList();
    }

    @JsonProperty("error")
    public String getError()
    {
        return message;
    }
}
