package com.ning.atlas.main;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestProvisionCommand
{
    @Test
    public void testFoo() throws Exception
    {
        MainOptions opts = new MainOptions("-e", "src/test/ruby/ex1/static-tagged.rb",
                                           "-s", "src/test/ruby/ex1/static-tagged.rb",
                                           "provision");

        opts.getCommand().create(opts).run();
        assertEquals(2, 2);
    }
}
