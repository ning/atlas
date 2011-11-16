package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Identity;

import java.util.List;
import java.util.Map;

public class HostDeploymentDescription
{
    private final Map<StepType, List<String>> steps = Maps.newEnumMap(StepType.class);
    private final Identity id;

    public HostDeploymentDescription(Identity id) {
        this.id = id;
        for (StepType stepType : StepType.values()) {
            steps.put(stepType, Lists.<String>newArrayList());
        }
    }

    public Map<StepType, List<String>> getSteps()
    {
        return steps;
    }

    public Identity getId()
    {
        return id;
    }

    void addStep(StepType type, String description)
    {
        steps.get(type).add(description);
    }
}
