package com.ning.atlas;

import com.ning.atlas.tree.Tree;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"type", "name", "my", "children"})
public class InstalledElement implements Tree<InstalledElement>
{
    private final String type;
    private final String name;
    private final My my;

    public InstalledElement(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
    }

    @Override
    public List<? extends InstalledElement> getChildren()
    {
        return Collections.emptyList();
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
    public static InstalledElement create(@JsonProperty("name") String name,
                                           @JsonProperty("type") String type,
                                           @JsonProperty("my") Map<String, Object> my,
                                           @JsonProperty("children") List<InstalledElement> children,
                                           @JsonProperty("server") Server server)
    {

        if (server != null) {
            return new InstalledServer(type, name, new My(my), server);
        }
        else {
            return new InstalledSystem(type, name, new My(my), children);
        }
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, true, Object.class);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }
}
