package com.ning.atlas;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;

public class Initialization
{
    private final String scheme;
    private final String fragment;
    private final String uri;

    private Initialization(String uri, String scheme, String fragment) {
        this.scheme = scheme;
        this.fragment = fragment;
        this.uri = uri;
    }

    public String getScheme()
    {
        return scheme;
    }

    public String getFragment()
    {
        return fragment;
    }

    public String getUriForm()
    {
        return uri;
    }

    public String toString() {
        return uri;
    }

    private static final Splitter splitter = Splitter.on(':');
    private static final Joiner joiner = Joiner.on(":");

    public static Initialization parseUriForm(String uri) {
        return valueOf(uri);
    }

    public static Initialization valueOf(String input) {
        Iterable<String> parts = splitter.split(input);
        Iterator<String> itty = parts.iterator();
        String scheme = itty.next();
        String fragment = joiner.join(ImmutableList.copyOf(itty));
        return new Initialization(input, scheme, fragment);
    }
}
