package com.ning.atlas;

import com.ning.atlas.spi.Uri;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.STException;

import java.util.Map;

public class UriTemplate<T>
{
    private final Uri<T> uri;

    public UriTemplate(Uri<T> uri)
    {
        this.uri = uri;
    }

    public Uri<T> apply(Map<String, Object> args)
    {
        try {
            ST st = new ST(uri.toStringUnEscaped(), '{', '}');
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                st.add(entry.getKey(), entry.getValue());
            }
            return Uri.valueOf(st.render());
        }
        catch (STException e) {
            throw new IllegalStateException("Bad stringtemplate evaluation of " + uri + " with " + args, e);
        }
    }
}
