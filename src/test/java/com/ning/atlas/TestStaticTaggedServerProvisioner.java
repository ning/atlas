package com.ning.atlas;

import com.google.common.util.concurrent.MoreExecutors;
import com.ning.atlas.tree.Trees;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TestStaticTaggedServerProvisioner
{

    private static JRubyTemplateParser parser = new JRubyTemplateParser();

    @Test
    public void testFoo() throws Exception
    {
        Environment e = parser.parseEnvironment(new File("src/test/ruby/ex1/static-tagged.rb"));
        Template root = parser.parseSystem(new File("src/test/ruby/ex1/static-tagged.rb"));

        BoundTemplate bt = root.normalize(e);

        ProvisionedElement pt = bt.provision(MoreExecutors.sameThreadExecutor()).get();

        List<ProvisionedElement> leaves = Trees.leaves(pt);
        assertThat(leaves.size(), equalTo(4));

        assertThat(leaves.get(0), instanceOf(ProvisionedServer.class));
        ProvisionedServer pst = (ProvisionedServer) leaves.get(0);
        assertThat(pst.getServer().getExternalAddress(), equalTo("10.0.0.1"));

    }
}
