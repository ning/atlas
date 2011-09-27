package com.ning.atlas.spi;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@JsonSerialize(using = Server.Serializer.class)
@JsonDeserialize(using = Server.Deserializer.class)
public final class Server
{
    private final String internalIp;
    private final String externalIp;
    private final Map<String, String> attributes = Maps.newConcurrentMap();

    public Server(String internalIp, String externalIp) {
        this(internalIp, externalIp, Collections.<String, String>emptyMap());
    }

    public Server(String internalIp,
                  String externalIp,
                  Map<String, String> attrs)
    {
        this.internalIp = internalIp;
        this.externalIp = externalIp;
        this.attributes.putAll(attrs);
    }

    @JsonProperty("external_address")
    public String getExternalAddress()
    {
        return externalIp;
    }

    @JsonProperty("internal_address")
    public String getInternalAddress()
    {
        return internalIp;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class Deserializer extends JsonDeserializer<Server> {

        @Override
        public Server deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
        {
            JsonNode node = jp.readValueAsTree();

            Map<String, String> attributes = Maps.newHashMap();
            for (Iterator<String> field_names = node.getFieldNames(); field_names.hasNext();) {
                String name= field_names.next();
                attributes.put(name, node.get(name).getTextValue());
            }
            String external_address = attributes.remove("external_address");
            String internal_address = attributes.remove("internal_address");


            return new Server(internal_address, external_address, attributes);
        }
    }

    public static class Serializer extends JsonSerializer<Server>
    {

        @Override
        public void serialize(Server value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
        {
            jgen.writeStartObject();

            jgen.writeFieldName("internal_address");
            jgen.writeString(value.getInternalAddress());

            jgen.writeFieldName("external_address");
            jgen.writeString(value.getExternalAddress());

            for (Map.Entry<String, String> entry : value.getAttributes().entrySet()) {
                jgen.writeFieldName(entry.getKey());
                jgen.writeString(entry.getValue());
            }

            jgen.writeEndObject();
        }
    }
}
