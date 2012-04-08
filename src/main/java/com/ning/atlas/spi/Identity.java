package com.ning.atlas.spi;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.Iterator;

@JsonSerialize(using = Identity.Serializer.class)
public final class Identity
{
    private final String externalForm;
    private final String type;
    private final String name;

    private Identity(Identity parent, String type, String name)
    {
        this.type = type;
        this.name = name;
        if (parent == null) {
            this.externalForm = "/";
        }
        else {
            StringBuilder b = new StringBuilder();
            if (!parent.toExternalForm().equals("/")) {
                b.append(parent.externalForm);
            }
            b.append("/").append(type).append(".").append(name);
            this.externalForm = b.toString();
        }
    }

    public boolean isRoot()
    {
        return this.externalForm.equals("/");
    }


    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(externalForm).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (! (obj instanceof Identity )) {
            return false;
        }

        Identity other = (Identity) obj;

        return externalForm.equals(other.externalForm);
    }

    public static Identity root()
    {
        return new Identity(null, "", "");
    }

    public Identity createChild(String type, String name)
    {
        return new Identity(this, type, name);
    }

    public String toExternalForm()
    {
        return externalForm;
    }

    public boolean isParentOf(Identity possibleChild)
    {
        return possibleChild.toExternalForm().startsWith(toExternalForm() + "/");
    }

    public String toString()
    {
        return toExternalForm();
    }

    private static final Splitter parentSplitter = Splitter.on('/');
    private static final Splitter partSplitter   = Splitter.on('.');

    public static Identity valueOf(String id)
    {
        Iterable<String> parents = parentSplitter.split(id);
        Identity root = Identity.root();
        for (String parent : parents) {
            Iterator<String> type_and_name = partSplitter.split(parent).iterator();
            if (type_and_name.hasNext() && !StringUtils.isEmpty(parent)) {
                String type = type_and_name.next();
                String name = type_and_name.next();
                if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(name)) {
                    root = root.createChild(type, name);
                }
            }
        }
        return root;
    }

    public static class Serializer extends JsonSerializer<Identity>
    {

        @Override
        public void serialize(Identity value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
        {
            jgen.writeString(value.toExternalForm());
        }
    }
}
