package com.ning.atlas.spi;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonSerialize(using = Uri.UriJsonSerializer.class)
public class Uri<T>
{
    private static final Pattern CONTAINS_TEMPLATE = Pattern.compile(".*\\{.+\\}.*");

    private final String                          scheme;
    private final String                          fragment;
    private final Map<String, Collection<String>> params;
    private final boolean                         isTemplate;

    public Uri(String scheme, String fragment)
    {
        this(scheme, fragment, Collections.<String, Collection<String>>emptyMap());
    }

    public Uri(String scheme, String fragment, Map<String, Collection<String>> params)
    {
        checkNotNull(scheme, "scheme may not be null");
        checkNotNull(fragment, "fragment may not be null");

        this.fragment = fragment.trim();
        String m_scheme = scheme.trim();

        boolean is_template = false;
        boolean force_not_template = false;

        if (m_scheme.startsWith("!")) {
            force_not_template = true;
            this.scheme = m_scheme.substring(1);
        }
        else {
            this.scheme = m_scheme;
        }


        Map<String, Collection<String>> tmp = Maps.newLinkedHashMap();
        for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
            List<String> values = ImmutableList.copyOf(entry.getValue());

            if ((!force_not_template) && CONTAINS_TEMPLATE.matcher(entry.getKey()).matches()) {
                is_template = true;
            }

            if (!force_not_template) {
                for (String value : values) {
                    if (CONTAINS_TEMPLATE.matcher(value).matches()) {
                        is_template = true;
                    }
                }
            }

            tmp.put(entry.getKey(), values);
        }
        this.params = ImmutableMap.copyOf(tmp);

        if ((!force_not_template)
            && (CONTAINS_TEMPLATE.matcher(scheme).matches() ||
                CONTAINS_TEMPLATE.matcher(fragment).matches()))
        {
            is_template = true;
        }

        isTemplate = is_template && (!force_not_template);

    }

    public boolean isTemplate()
    {
        return isTemplate;
    }

    public String getScheme()
    {
        return scheme;
    }

    public String getFragment()
    {
        return fragment;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder().append(scheme);
        if (fragment.length() > 0) {
            builder.append(":").append(fragment);
        }

        if (!params.isEmpty()) {
            List<NameValuePair> pairs = Lists.newArrayList();
            for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
                String key = entry.getKey();
                for (String value : entry.getValue()) {
                    pairs.add(new BasicNameValuePair(key, value));
                }
            }

            builder.append("?").append(URLEncodedUtils.format(pairs, "UTF8"));
        }
        return builder.toString();
    }

    public String toStringUnEscaped() {
         StringBuilder builder = new StringBuilder().append(scheme);
        if (fragment.length() > 0) {
            builder.append(":").append(fragment);
        }

        if (!params.isEmpty()) {
            builder.append("?");
            List<String> parts = Lists.newArrayList();
            for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
                for (String value : entry.getValue()) {
                    parts.add(entry.getKey() + "=" + value);
                }
            }
            builder.append(Joiner.on("&").join(parts));
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof Uri && EqualsBuilder.reflectionEquals(this, o);

    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static <T> Uri<T> valueOf2(String uri, @Nullable Map<Object, Object> additionalParams)
    {
        Multimap<String, String> mmap = ArrayListMultimap.create();
        if (additionalParams != null) {
            for (Map.Entry<Object, Object> entry : additionalParams.entrySet()) {
                mmap.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return valueOf(uri, mmap.asMap());
    }

    public static <T> Uri<T> valueOf(String uri, Map<String, Collection<String>> additionalParams)
    {
        checkNotNull(uri);

        int q_index = uri.indexOf('?');
        Multimap<String, String> p = ArrayListMultimap.create();

        if (q_index >= 0) {
            List<NameValuePair> pairs = URLEncodedUtils.parse(URI.create(uri.substring(q_index)), "UTF8");
            for (NameValuePair pair : pairs) {
                p.put(pair.getName(), pair.getValue());
            }
        }
        for (Map.Entry<String, Collection<String>> entry : additionalParams.entrySet()) {
            p.putAll(entry.getKey(), entry.getValue());
        }

        int colon_index = uri.indexOf(':');
        if (colon_index > 0 && q_index > 0) {
            return new Uri<T>(uri.substring(0, colon_index), uri.substring(colon_index + 1, q_index), p.asMap());
        }
        else if (colon_index > 0) {
            return new Uri<T>(uri.substring(0, colon_index), uri.substring(colon_index + 1), p.asMap());
        }
        else {
            return new Uri<T>(uri, "", p.asMap());
        }
    }

    @JsonCreator
    public static <T> Uri<T> valueOf(String uri)
    {
        boolean has_args = false;
        Multimap<String, String> params = TreeMultimap.create();
        if (uri.contains("?")) {
            has_args = true;

            String query = uri.substring(uri.indexOf("?")+1);
            Map<String, String> bits = Splitter.on('&').withKeyValueSeparator("=").split(query);
            for (Map.Entry<String, String> entry : bits.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
        }

        return valueOf(has_args ? uri.substring(0, uri.indexOf("?")) : uri, params.asMap());
    }

    public Map<String, Collection<String>> getFullParams()
    {
        return params;
    }

    public Map<String, String> getParams()
    {
        return Maps.transformValues(getFullParams(), new Function<Collection<String>, String>()
        {
            @Override
            public String apply(@Nullable Collection<String> input)
            {
                return input == null || input.isEmpty() ? null : input.iterator().next();
            }
        });
    }

    public static <T> Function<? super String, ? extends Uri<T>> stringToUri()
    {
        return new Function<String, Uri<T>>()
        {
            @Override
            public Uri<T> apply(@Nullable String s)
            {
                return Uri.valueOf(s);
            }
        };
    }

    public static Function<? super Uri<?>, ? extends String> urisToStrings()
    {
        return new Function<Uri<?>, String>()
        {
            @Override
            public String apply(Uri<?> uri)
            {
                return uri.toString();
            }
        };
    }

    public static class UriJsonSerializer extends JsonSerializer<Uri>
    {

        @Override
        public void serialize(Uri value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
        {
            jgen.writeString(value.toString());
        }
    }
}
