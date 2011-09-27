package com.ning.atlas.plugin;

import com.ning.atlas.spi.Installer;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClasspathArchive;

import javax.annotation.Nullable;
import java.util.List;

public class Plugins
{
    public static Class<Installer> findByName(String name) {
        AnnotationFinder finder = new AnnotationFinder(new ClasspathArchive(Plugins.class.getClassLoader()));
        List<Class<?>> classes = finder.findAnnotatedClasses(Nullable.class);
        return (Class<Installer>) classes.get(0);
    }
}
