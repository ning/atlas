package com.ning.atlas;

import com.ning.atlas.tree.Trees;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FancyInstaller implements Installer
{
    @Override
    public void install(Server server, String fragment, InitializedTemplate root) throws Exception
    {
        List<InitializedServer> candidates = Trees.findInstancesOf(root, InitializedServer.class);
        boolean found = false;
        for (InitializedServer candidate : candidates) {
            found = found || "yes".equals(candidate.getMy().get("butter"));
        }
        assertThat(found, equalTo(true));
    }
}
