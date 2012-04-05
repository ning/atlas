package com.ning.atlas.components;

import com.google.common.io.CharStreams;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.InputStreamReader;

public class TestSSHKeyPairGenerator
{
    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        //      ssh-keygen [-q] [-b bits] -t type [-N new_passphrase] [-C comment] [-f output_keyfile]
        Process p = new ProcessBuilder("ssh-keygen",
                                       "-b", "1024",
                                       "-t", "rsa",
                                       "-f", "/tmp/foo").start();

        int exit = p.waitFor();
        if (exit != 0) {
            System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
            System.out.println(CharStreams.toString(new InputStreamReader(p.getErrorStream())));
        }
    }

    @Test

    @Ignore
    public void testBar() throws Exception
    {
        System.out.println(new File("tar").getAbsolutePath());
    }


}
