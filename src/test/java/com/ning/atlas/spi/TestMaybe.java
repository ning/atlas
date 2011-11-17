package com.ning.atlas.spi;

import com.google.common.base.Function;
import org.junit.Test;

public class TestMaybe
{
    @Test
    public void testFoo() throws Exception
    {
        Maybe<String> name = Maybe.definitely("Brian");
        Maybe<String> message = name.to(new Function<String, String>() {
            public String apply(String s)
            {
                return "hello, " + s;
            }
        });
        System.out.println(message.otherwise("No one here!"));
        // -> hello world
    }
}
