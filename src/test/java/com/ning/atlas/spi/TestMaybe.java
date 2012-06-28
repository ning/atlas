package com.ning.atlas.spi;

import com.google.common.base.Function;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestMaybe
{
    @Test
    public void testFoo() throws Exception
    {
        Maybe<String> name = Maybe.definitely("Brian");
        Maybe<String> message = name.to(new Function<String, String>()
        {
            public String apply(String s)
            {
                return "hello, " + s;
            }
        });
        System.out.println(message.otherwise("No one here!"));
        // -> hello world
    }

    @Test
    public void testJrubyHackery() throws Exception
    {
        Maybe<String> m = Maybe.definitely("hello world");
        ScriptingContainer container = new ScriptingContainer();
        container.put("m", m);
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);

        String out = String.valueOf(container.runScriptlet("m.otherwise { 'woof' }"));
        assertThat(out, equalTo("hello world"));
    }

    @Test
    public void testJrubyHackery2() throws Exception
    {
        Maybe<String> m = Maybe.unknown();
        ScriptingContainer container = new ScriptingContainer();
        container.put("m", m);
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);
        try {
            container.runScriptlet("m.elsewise { raise 'noooo' }");
            fail("should have raised exception");
        }
        catch (Exception e) {
            assertThat(e.getMessage(), equalTo("(RuntimeError) noooo"));
        }
    }
}
