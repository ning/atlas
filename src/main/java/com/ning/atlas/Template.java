package com.ning.atlas;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.My;
import com.ning.atlas.tree.Tree;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class Template implements Tree
{
    private final List<String> cardinality;
    private final String type;
    private final My my;

    public Template(String type, My my, List<?> cardinality)
    {
        Preconditions.checkArgument(!type.contains("."), "type is not allowed to contain '.' but is '%s'", type);
        Preconditions.checkArgument(!type.contains("/"), "type is not allowed to contain '/' but is '%s'", type);

        this.my = my;
        this.type = type;
        this.cardinality = Lists.transform(cardinality, new Function<Object, String>()
        {
            @Override
            public String apply(@Nullable Object input)
            {
                return String.valueOf(input);
            }
        });
    }

    public String getType()
    {
        return type;
    }

    public List<String> getCardinality()
    {
        for (String s : cardinality) {
            checkArgument(!s.contains("."), "cardinality values are not allowed to contain '.' but has '%s'", s);
            checkArgument(!s.contains("/"), "cardinality values are not allowed to contain '/' but has '%s'", s);
        }
        return this.cardinality;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public My getMy()
    {
        return my;
    }

    public final SystemMap normalize() {
        return new SystemMap(_nom(Identity.root()));
    }

    protected abstract List<Element> _nom(Identity parent);

}
