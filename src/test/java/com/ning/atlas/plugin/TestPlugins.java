package com.ning.atlas.plugin;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.archive.JarArchive;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestPlugins
{
    @Test
    public void testClassloaderFun() throws Exception
    {
        assumeThat(1+1, equalTo(3));
        String pwd = System.getProperty("user.dir");
        PluginDirectory pd = new PluginDirectory(new File("src/test/test_plugins"));
        String s_url = "jar:file://" + pwd + "/src/test/resources/test_plugins/silly.jar!/";
        URL url = new URL(s_url);
        URLClassLoader ucl = new URLClassLoader(new URL[] { url });
        JarArchive archive = new JarArchive(ucl, url);
        Class<Callable<String>> s = (Class<Callable<String>>) archive.loadClass("Silly");
        Callable<String> c = s.newInstance();
        assertThat(c.call(), equalTo("Silly"));

        ClassFinder finder = new ClassFinder(ucl);
        List<Class<? extends Callable>> rs = finder.findImplementations(Callable.class);
        assertThat((Object)rs, equalTo((Object)asList(s)));
    }
}
