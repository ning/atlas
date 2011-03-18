package com.ning.atlas.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deployment
{
    private final List<Instance> instances = new ArrayList<Instance>();

    public List<Instance> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }

    public void addInstance(Instance instance)
    {
        this.instances.add(instance);
    }

    public List<Instance> validate()
    {
        List<Instance> bads = new ArrayList<Instance>();
        for (Instance instance : instances) {
            List<String> problems = instance.validate();
            if (!problems.isEmpty()) {
                bads.add(instance);
            }
        }

        return bads;
    }
}
