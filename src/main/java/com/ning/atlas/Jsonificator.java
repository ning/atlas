package com.ning.atlas;

import com.ning.atlas.spi.Node;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;

public class Jsonificator
{
    private final static ObjectMapper mapper = new ObjectMapper();

    static { mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true); }

    public static String jsonify(Node template ) throws IOException
    {
        return mapper.writeValueAsString(template);
    }
}
