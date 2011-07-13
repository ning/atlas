package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.tree.Trees;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ning.atlas.testing.AtlasMatchers.containsInstanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class TestProvisioning
{
    @Test
    public void testStaticTaggedProvisioning() throws Exception
    {
        final Provisioner p = new StaticTaggedServerProvisioner(new HashMap<String, Collection<String>>()
        {{ put("concrete", Arrays.asList("10.0.0.1")); }});

        Base base = new Base("concrete",
                             new Environment("tests")
                             {{ setProvisioner(p); }},
                             new HashMap<String, String>()
                             {{put("tag", "concrete");}}
        );

        BoundServerTemplate child = new BoundServerTemplate("child", "0", new My(), base, Collections.<String>emptyList());

        BoundTemplate root = new BoundSystemTemplate("root", "1", new My(), Arrays.<BoundTemplate>asList(child));

        ListenableFuture<? extends ProvisionedTemplate> rs = root.provision(Executors.newFixedThreadPool(2));
        ProvisionedTemplate proot = rs.get();

        List<ProvisionedTemplate> leaves = Trees.leaves(proot);
        assertThat(leaves.size(), equalTo(1));
        assertThat(leaves.get(0), instanceOf(ProvisionedServerTemplate.class));

        ProvisionedServerTemplate pst = (ProvisionedServerTemplate) leaves.get(0);
        assertThat(pst.getServer().getExternalAddress(), equalTo("10.0.0.1"));
    }

    @Test
    public void testUnableToFindNeededServer() throws Exception
    {
        final Provisioner p = new StaticTaggedServerProvisioner(new HashMap<String, Collection<String>>()
        {{ put("concrete", Arrays.asList("10.0.0.1")); }});

        Base base1 = new Base("concrete",
                              new Environment("tests")
                              {{ setProvisioner(p); }},
                              new HashMap<String, String>()
                              {{put("tag", "concrete");}}
        );
        BoundServerTemplate child = new BoundServerTemplate("child", "0", new My(), base1, Collections.<String>emptyList());


        BoundServerTemplate child2 = new BoundServerTemplate("child", "1", new My(), base1, Collections.<String>emptyList());

        BoundTemplate root = new BoundSystemTemplate("root", "0", new My(), Arrays.<BoundTemplate>asList(child, child2));

        ExecutorService ex = Executors.newFixedThreadPool(2);
        ListenableFuture<? extends ProvisionedTemplate> rs = root.provision(ex);
        ProvisionedTemplate proot = rs.get();

        List<ProvisionedTemplate> leaves = Trees.leaves(proot);
        assertThat(leaves.size(), equalTo(2));

        assertThat(leaves, containsInstanceOf(ProvisionedServerTemplate.class));
        assertThat(leaves, containsInstanceOf(ProvisionedErrorTemplate.class));
        ex.shutdown();
    }
}
