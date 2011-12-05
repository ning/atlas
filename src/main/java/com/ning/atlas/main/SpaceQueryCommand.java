package com.ning.atlas.main;

import com.google.common.base.Joiner;
import com.ning.atlas.Environment;
import com.ning.atlas.Host;
import com.ning.atlas.JRubyTemplateParser;
import com.ning.atlas.SystemMap;
import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

public class SpaceQueryCommand implements Callable<Void>
{
    private final MainOptions mo;

    public SpaceQueryCommand(MainOptions mo)
    {
        this.mo = mo;
    }

    @Override
    public Void call() throws Exception
    {

        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));

        String q = mo.getCommandArguments()[0];

        for (String result : space.query(q)) {
            System.out.println(result);
        }

        return null;

    }
}
