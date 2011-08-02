package com.ning.atlas;

import com.ning.atlas.tree.Tree;
import com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"type", "name", "my", "children"})
public class InstalledTemplate implements Tree<InstalledTemplate>
{
    private final String type;
    private final String name;
    private final My my;

    public InstalledTemplate(String type, String name, My my)
    {
        this.type = type;
        this.name = name;
        this.my = my;
    }

    @Override
    public List<? extends InstalledTemplate> getChildren()
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
    public static InstalledTemplate create(@JsonProperty("name") String name,
                                           @JsonProperty("type") String type,
                                           @JsonProperty("my") Map<String, Object> my,
                                           @JsonProperty("children") List<InstalledTemplate> children,
                                           @JsonProperty("server") Server server)
    {

        if (children == null) {
            return new InstalledServerTemplate(type, name, new My(my), server);
        }
        else {
            return new InstalledSystemTemplate(type, name, new My(my), children);
        }


    }
}
