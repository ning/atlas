package com.ning.atlas.badger;

public class Uri<T>
{
    private final String scheme;
    private final String fragment;

    public Uri(String scheme, String fragment)
    {
        this.scheme = scheme;
        this.fragment = fragment;
    }

    public String getScheme()
    {
        return scheme;
    }

    public String getFragment()
    {
        return fragment;
    }

    public static <T> Uri<T> valueOf(String uri) {
        int colon_index = uri.indexOf(':');
        if (colon_index >= 0) {
            return new Uri(uri.substring(0, colon_index), uri.substring(colon_index + 1));
        }
        else {
            return new Uri<T>(uri, "");
        }
    }

}
