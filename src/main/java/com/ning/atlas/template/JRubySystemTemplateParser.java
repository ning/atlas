package com.ning.atlas.template;

import com.google.common.io.Resources;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class JRubySystemTemplateParser
{
    public ConfigurableSystemTemplate parse(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        try {
            container.runScriptlet(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                      Charset.defaultCharset()));
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (ConfigurableSystemTemplate) container.runScriptlet("Atlas.parse_system('" + template.getAbsolutePath() + "')");
    }
}
