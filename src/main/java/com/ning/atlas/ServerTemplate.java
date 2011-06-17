package com.ning.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ServerTemplate extends Template
{
    private List<String> installations = new ArrayList<String>();
    private String base;

    public ServerTemplate(String name)
    {
        super(name);
    }

    @Override
    protected final Iterable<BoundTemplate> normalize(Environment env, Stack<String> names)
    {
        names.push(getType());
        final List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {
            rs.add(new BoundServerTemplate(this, node_name, env, names));
        }
        names.pop();
        return rs;
    }

    public Iterable<? extends Template> getChildren()
    {
        return Collections.emptyList();
    }

    public List<String> getInstallations()
    {
        return installations;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public String getBase()
    {
        return base;
    }

    /**
     * called by jruby template parser
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setInstall(List<String> installs)
    {
        this.installations = new ArrayList<String>(installs);
    }
}
