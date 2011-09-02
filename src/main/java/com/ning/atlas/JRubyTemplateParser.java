package com.ning.atlas;

import com.google.common.io.Resources;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

public class JRubyTemplateParser
{
    public Template parseSystem(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompatVersion(CompatVersion.RUBY1_9);
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        try {
            container.runScriptlet(new StringReader(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                                       Charset.defaultCharset())), "atlas/parser.rb");
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (Template) container.runScriptlet("Atlas.parse_system('" + template.getAbsolutePath() + "')");
    }

    public Environment parseEnvironment(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompatVersion(CompatVersion.RUBY1_9);
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);

        try {
            container.runScriptlet(new StringReader(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                                       Charset.defaultCharset())), "atlas/parser.rb");
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (Environment) container.runScriptlet("Atlas.parse_env('" + template.getAbsolutePath() + "')");
    }
}
