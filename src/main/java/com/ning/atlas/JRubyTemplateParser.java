package com.ning.atlas;

import com.google.common.io.Resources;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

public class JRubyTemplateParser
{
    @SuppressWarnings("unchecked")
	public List<Template> parseSystem(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);
        try {
            container.runScriptlet(new StringReader(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                                       Charset.defaultCharset())), "atlas/parser.rb");
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (List<Template>) container.runScriptlet("Atlas.parse_system('" + template.getAbsolutePath() + "')");
    }

    @SuppressWarnings("unchecked")
	public List<Environment> parseEnvironment(File template)
    {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);

        try {
            container.runScriptlet(new StringReader(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                                       Charset.defaultCharset())), "atlas/parser.rb");
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (List<Environment>) container.runScriptlet("Atlas.parse_env('" + template.getAbsolutePath() + "')");
    }

    public Descriptor parseDescriptor(File descriptor)
    {
        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.OFF);
        container.setCompatVersion(CompatVersion.RUBY1_9);
        try {
            container.runScriptlet(new StringReader(Resources.toString(Resources.getResource("atlas/parser.rb"),
                                                                       Charset.defaultCharset())), "atlas/parser.rb");
        }
        catch (IOException e) {
            throw new IllegalStateException("cannot open atlas/parser.rb from classpath", e);
        }

        return (Descriptor) container.runScriptlet("Atlas.parse_descriptor('" + descriptor.getAbsolutePath() + "')");
    }
}
