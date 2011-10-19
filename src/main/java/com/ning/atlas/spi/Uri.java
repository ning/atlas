package com.ning.atlas.spi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Uri<T>
{
    private final String                          scheme;
    private final String                          fragment;
    private final Map<String, Collection<String>> params;

    public Uri(String scheme, String fragment)
    {
        this(scheme, fragment, Collections.<String, Collection<String>>emptyMap());
    }

    public Uri(String scheme, String fragment, Map<String, Collection<String>> params)
    {
        checkNotNull(scheme, "scheme may not be null");
        checkNotNull(fragment, "fragment may not be null");

        this.scheme = scheme;
        this.fragment = fragment;
        Map<String, Collection<String>> tmp = Maps.newLinkedHashMap();
        for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
            List<String> values = ImmutableList.copyOf(entry.getValue());
            tmp.put(entry.getKey(), values);
        }
        this.params = ImmutableMap.copyOf(tmp);
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

    public static <T> Uri<T> valueOf(String uri)
    {
        return valueOf(uri, Collections.<String, Collection<String>>emptyMap());
    }

    public Map<String, Collection<String>> getParams()
    {
        return params;
    }
}
