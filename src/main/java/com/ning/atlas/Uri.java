package com.ning.atlas;

import static com.google.common.base.Preconditions.checkNotNull;

public class Uri<T>
{
    private final String scheme;
    private final String fragment;

    public Uri(String scheme, String fragment)
    {
        checkNotNull(scheme, "scheme may not be null");
        checkNotNull(fragment, "fragment may not be null");

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

    @Override
    public String toString()
    {
        return new StringBuilder().append(scheme).append(":").append(fragment).toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Uri uri = (Uri) o;
        return fragment.equals(uri.fragment) && scheme.equals(uri.scheme);

    }

    @Override
    public int hashCode()
    {
        int result = scheme.hashCode();
        result = 31 * result + fragment.hashCode();
        return result;
    }

    public static <T> Uri<T> valueOf(String uri) {
        checkNotNull(uri);

        int colon_index = uri.indexOf(':');
        if (colon_index >= 0) {
            return new Uri<T>(uri.substring(0, colon_index), uri.substring(colon_index + 1));
        }
        else {
            return new Uri<T>(uri, "");
        }
    }

}
