package com.ning.atlas.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.Console;
import java.io.PrintWriter;

public class ColorizedAppender extends AppenderSkeleton
{
    private final PrintWriter writer;
    private final Wrapper     error;
    private final Wrapper     warn;
    private final Wrapper     info;
    private final Wrapper     debug;

    public ColorizedAppender()
    {
        Console console = System.console();
        if (console == null) {
            error = warn = info = debug = NO_OP;
            this.writer = new PrintWriter(System.out);
        }
        else {
            error = new ColorWrapper("31");
            warn = new ColorWrapper("33");
            info = new ColorWrapper("32");
            debug = new ColorWrapper("34");
            this.writer = console.writer();
        }
    }

    private String format(Wrapper w, LoggingEvent msg) {
        return w.start() + this.getLayout().format(msg) + w.end();
    }

    @Override
    protected void append(LoggingEvent event)
    {
        final String line;
        switch (event.getLevel().toInt()) {
            case Level.ERROR_INT:
                line = format(error, event);
                break;
            case Level.WARN_INT:
                line = format(warn, event);
                break;
            case Level.INFO_INT:
                line = format(info, event);
                break;
            case Level.DEBUG_INT:
                line = format(debug, event);
                break;
            default:
                line = format(NO_OP, event);
                break;
        }
        writer.write(line);
        writer.flush();
    }

    @Override
    public boolean requiresLayout()
    {
        return true;
    }

    @Override
    public void close()
    {

    }

    private interface Wrapper
    {
        String start();

        String end();
    }

    private static final Wrapper NO_OP = new Wrapper()
    {

        @Override
        public String start()
        {
            return "";
        }

        @Override
        public String end()
        {
            return "";
        }
    };

    private static class ColorWrapper implements Wrapper
    {

        private final String color;

        ColorWrapper(String color)
        {

            this.color = "\033[" + color + "m";
        }

        @Override
        public String start()
        {
            return color;
        }

        @Override
        public String end()
        {
            return "\033[0m";
        }
    }
}
