package com.ning.atlas.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestMain
{
    @Test
    public void testOptionParserExample() throws Exception
    {
        final OptionParser parser = new OptionParser();
        parser.posixlyCorrect(true);

        OptionSpec<String> e = parser.acceptsAll(asList("e", "env", "environment"), "Environment specification file")
                                     .withRequiredArg()
                                     .ofType(String.class);
        OptionSpec<String> s = parser.acceptsAll(asList("s", "sys", "system"), "System specification file")
                                     .withRequiredArg()
                                     .ofType(String.class);

        OptionSet o = parser.parse("-e", "hello", "-s", "world", "provision", "--wombat");

        assertThat(o.valueOf(e), equalTo("hello"));
        assertThat(o.valueOf(s), equalTo("world"));
        assertThat(asList("provision", "--wombat"), equalTo(o.nonOptionArguments()));
    }

    @Test
    public void testMainOptions() throws Exception
    {
        MainOptions mo = new MainOptions("-s", "/sys_file", "-e", "/env_file", "help", "--verbose");


        assertThat(mo.getEnvironmentPath(), equalTo("/env_file"));
        assertThat(mo.getSystemPath(), equalTo("/sys_file"));

        assertThat(mo.getCommand(), equalTo(Command.help));
        assertThat(mo.getCommandArguments(), equalTo(new String[] {"--verbose"}));
    }
}
