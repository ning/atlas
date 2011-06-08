package com.ning.atlas.testing;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.File;

public class FileMatchers
{
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
