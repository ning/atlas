package com.ning.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ning.atlas.spi.Identity;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

public class Descriptor
{
    private final List<Template>           templates    = Lists.newArrayList();
    private final Map<String, Environment> environments = Maps.newLinkedHashMap();

    public Descriptor(Iterable<Template> templates, Iterable<Environment> environments)
    {
        Iterables.addAll(this.templates, templates);
        for (Environment environment : environments) {
            if (this.environments.containsKey(environment.getName())) {
                throw new IllegalStateException("Environments must be uniquely named, there is a duplicate " +
                                                environment.getName() + "  environment being defined");
            }
            else {
                this.environments.put(environment.getName(), environment);
            }
        }
    }

    public List<Template> getTemplates()
    {
        return Collections.unmodifiableList(templates);
    }

    public List<Environment> getEnvironments()
    {
        return ImmutableList.copyOf(environments.values());
    }

    public Descriptor combine(Descriptor other)
    {
        return new Descriptor(Iterables.concat(this.getTemplates(), other.getTemplates()),
                              Iterables.concat(this.getEnvironments(), other.getEnvironments()));
    }

    public SystemMap normalize(String environmentName)
    {
        Environment env = environments.get(environmentName);
        checkNotNull(env, "No environment named '%s' available", environmentName);
        Iterator<Template> itty = this.templates.iterator();
        if (!itty.hasNext()) {
            return new SystemMap();
        }

        SystemMap map = itty.next().normalize(env);
        while (itty.hasNext()) {
            map = map.combine(itty.next().normalize(env));
        }

        Collection<Template> env_defined = env.getEnvironmentDefinedElements();
        for (Template template : env_defined) {
            List<Element> el = template._normalize(Identity.root(), env);
            map = new SystemMap(concat(map.getRoots(), el));
        }

        return map;
    }

    public Environment getEnvironment(String test)
    {
        return environments.get(test);
    }

    public static Descriptor empty()
    {
        return new Descriptor(Collections.<Template>emptyList(), Collections.<Environment>emptyList());
    }
}
