package com.ning.atlas.errors;

import com.ning.atlas.logging.Logger;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class ErrorCollector
{
    private final Logger logger = Logger.get(ErrorCollector.class);
    private final List<Pair<Throwable, String>> errors = new CopyOnWriteArrayList<Pair<Throwable, String>>();

    public String error(Throwable e, String format, Object... args)
    {
        String msg;
        try {
            msg = String.format(format, args);
        }
        catch (IllegalFormatException ife) {
            msg = format("'%s' %s", format, asList(args));
            logger.warn(ife, "Illegal formatting for message: %s", msg);
        }
        errors.add(Pair.of(e, msg));
        return msg;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void dumpErrorsTo(PrintStream out)
    {
        out.printf("Errors while processing:\n");
        for (Pair<Throwable, String> error : errors) {
            //noinspection ThrowableResultOfMethodCallIgnored
            out.printf("\t%s\t%s\n", error.getRight(), error.getLeft().getMessage());
        }
    }
}
