package com.ning.atlas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerTemplate extends Template
{
    private List<String> installations = new ArrayList<String>();
    private String base;

    @Deprecated
    public ServerTemplate(String name) {
        this(name, Collections.<String, Object>emptyMap());
    }

    public ServerTemplate(String name, Map<String, Object> my)
    {
        super(name, new My(my));
    }

    @Override
    public final Iterable<BoundTemplate> _normalize(Environment env, Identity parent)
    {
        final List<BoundTemplate> rs = new ArrayList<BoundTemplate>();
        List<String> node_names = getCardinality();
        for (String node_name : node_names) {

            rs.add(new BoundServer(parent.createChild(getType(), node_name) , this, node_name, env, installations));
        }
        return rs;
    }

    public Collection<? extends Thing> getChildren()
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
    public void setInstall(List<String> installs)
    {
        this.installations = new ArrayList<String>(installs);
    }

}
