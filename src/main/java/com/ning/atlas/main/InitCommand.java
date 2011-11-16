package com.ning.atlas.main;

import com.ning.atlas.space.SQLiteBackedSpace;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.space.Space;

import java.io.File;
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

        space.store(ID, "system-path", new File(mainOptions.getSystemPath()).getAbsolutePath());
        space.store(ID, "environment-path", new File(mainOptions.getEnvironmentPath()).getAbsolutePath());

        return null;
    }
}
