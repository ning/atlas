package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.atlas.Base;
import com.ning.atlas.BoundServerTemplate;
import com.ning.atlas.BoundSystemTemplate;
import com.ning.atlas.BoundTemplate;
import com.ning.atlas.ProvisionedServerTemplate;
import com.ning.atlas.ProvisionedTemplate;
import com.ning.atlas.Provisioner;
import com.ning.atlas.StaticTaggedServerProvisioner;
import com.ning.atlas.tree.Trees;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestProvisioning
{
    @Test
    public void testStaticTaggedProvisioning() throws Exception
    {
        final Provisioner p = new StaticTaggedServerProvisioner(new HashMap<String, Collection<String>>()
        {{ put("concrete", Arrays.asList("10.0.0.1")); }});



        BoundServerTemplate child = new BoundServerTemplate("child", new Base("concrete",
                                                                              new Environment("tests") {{ setProvisioner(p); }},
                                                                              new HashMap<String, String>()
                                                                              {{put("tag", "concrete");}}));
        BoundTemplate root = new BoundSystemTemplate("root", Arrays.<BoundTemplate>asList(child));

        ListenableFuture<? extends ProvisionedTemplate> rs = root.provision(Executors.newFixedThreadPool(2));
        ProvisionedTemplate proot = rs.get();

        List<ProvisionedTemplate> leaves = Trees.leaves(proot);
        assertThat(leaves.size(), equalTo(1));
        assertThat(leaves.get(0), instanceOf(ProvisionedServerTemplate.class));

        ProvisionedServerTemplate pst = (ProvisionedServerTemplate) leaves.get(0);
        assertThat(pst.getExternalIP(), equalTo("10.0.0.1"));
    }
}
