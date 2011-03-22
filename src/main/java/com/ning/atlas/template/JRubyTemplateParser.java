package com.ning.atlas.template;

import com.google.common.io.Resources;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class JRubyTemplateParser
{
    public SystemTemplate parse(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        try {
            container.runScriptlet(Resources.toString(Resources.getResource("atlas/template.rb"),
                                                      Charset.defaultCharset()));
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/template.rb from classpath", e);
        }

        return (SystemTemplate) container.runScriptlet("Atlas::Template::SystemTemplateParser.new('" +
                                                       template.getAbsolutePath() +
                                                       "').parse");
    }
}
