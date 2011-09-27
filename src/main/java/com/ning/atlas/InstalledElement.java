package com.ning.atlas;

import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.spi.Node;
import com.ning.atlas.spi.Server;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"type", "name", "my", "children"})
public class InstalledElement implements Node
{
    private final Identity id;
    private final String   type;
    private final String   name;
    private final My       my;

    public InstalledElement(Identity id, String type, String name, My my)
    {
        this.id = id;
        this.type = type;
        this.name = name;
        this.my = my;
    }

    @Override
    public Collection<? extends InstalledElement> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public Identity getId()
    {
        return this.id;
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public My getMy()
    {
        return my;
    }

    @JsonCreator
    public static InstalledElement create(@JsonProperty("id") String id,
                                          @JsonProperty("name") String name,
                                          @JsonProperty("type") String type,
                                          @JsonProperty("my") Map<String, Object> my,
                                          @JsonProperty("children") List<InstalledElement> children,
                                          @JsonProperty("server") Server server)
    {

        if (server != null) {
            return new InstalledServer(Identity.valueOf(id), type, name, new My(my), server, Collections.<String, String>emptyMap());
        }
        else {
            return new InstalledSystem(Identity.valueOf(id), type, name, new My(my), children);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o, true, Object.class);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }
}
