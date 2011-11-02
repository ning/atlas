package com.ning.atlas.main;

import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class InitCommand implements Callable<Void>
{

    public static final Identity ID = Identity.root().createChild("atlas", "config").createChild("init", "global");
    private final MainOptions mainOptions;

    public InitCommand(MainOptions mainOptions) {
        this.mainOptions = mainOptions;
    }

    @Override
    public Void call() throws Exception
    {
        Space space = SQLiteBackedSpace.create(new File(".atlas", "space.db"));

        space.store(ID, "system-path", mainOptions.getSystemPath());
        space.store(ID, "environment-path", mainOptions.getEnvironmentPath());

        return null;
    }
}
