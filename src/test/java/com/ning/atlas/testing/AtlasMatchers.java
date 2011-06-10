package com.ning.atlas.testing;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.File;

public class AtlasMatchers
{

    public static Matcher<Iterable> containsInstanceOf(final Class<?> type) {
        return new BaseMatcher<Iterable>()
        {
            @Override
            public boolean matches(Object item)
            {
                Iterable it = (Iterable) item;
                for (Object o : it) {
                    if (type.isAssignableFrom(o.getClass())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("iterable to contain an instance of " + type.getName());
            }
        };
    }

    public static Matcher<File> exists()
    {
        return new BaseMatcher<File>()
        {
            public boolean matches(Object item)
            {
                File f = (File) item;
                return f.exists();
            }

            public void describeTo(Description description)
            {
                description.appendText("the expected file does not exist");
            }
        };
    }

}
