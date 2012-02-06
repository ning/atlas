package com.ning.atlas;

public class TestProvisioning
{
//    @Test
//    public void testStaticTaggedProvisioning() throws Exception
//    {
//        final Provisioner p = new StaticTaggedServerProvisioner(new HashMap<String, Collection<String>>()
//        {{ put("concrete", Arrays.asList("10.0.0.1")); }});
//
//        Base base = new Base("concrete",
//                             new Environment("tests",
//                                             ImmutableMap.<String, Provisioner>of("static", p),
//                                             Collections.<String, Installer>emptyMap()),
//                             "static",
//                             Collections.<Initialization>emptyList(),
//                             ImmutableMap.<String, String>of("tag", "concrete"));
//
//        BoundServer child = new BoundServer(Identity.root(), "child", "0", new My(), base, Collections.<String>emptyList());
//
//        BoundTemplate root = new BoundSystemTemplate(Identity.root(), "root", "1", new My(), Arrays.<BoundTemplate>asList(child));
//
//        ListenableFuture<? extends ProvisionedElement> rs = root.provision(new ErrorCollector(),
//                                                                           Executors.newFixedThreadPool(2));
//        ProvisionedElement proot = rs.get();
//
//        List<ProvisionedElement> leaves = Trees.leaves(proot);
//        assertThat(leaves.size(), equalTo(1));
//        assertThat(leaves.get(0), instanceOf(ProvisionedServer.class));
//
//        ProvisionedServer pst = (ProvisionedServer) leaves.get(0);
//        assertThat(pst.getServer().getExternalAddress(), equalTo("10.0.0.1"));
//    }

//    @Test
//    public void testUnableToFindNeededServer() throws Exception
//    {
//        final Provisioner p = new StaticTaggedServerProvisioner(
//            ImmutableMap.<String, Collection<String>>of("concrete", ImmutableList.<String>of("10.0.0.1")));
//
//        Base base1 = new Base("concrete",
//                              new Environment("tests",
//                                              ImmutableMap.<String, Provisioner>of("static", p),
//                                              Collections.<String, Installer>emptyMap()),
//                              "static",
//                              Collections.<Initialization>emptyList(),
//                              ImmutableMap.<String, String>of("tag", "concrete")
//        );
//        BoundServer child = new BoundServer(Identity.root(),"child", "0", new My(), base1, Collections.<String>emptyList());
//
//
//        BoundServer child2 = new BoundServer(Identity.root(),"child", "1", new My(), base1, Collections.<String>emptyList());
//
//        BoundTemplate root = new BoundSystemTemplate(Identity.root(),"root", "0", new My(), Arrays.<BoundTemplate>asList(child, child2));
//
//        ExecutorService ex = Executors.newFixedThreadPool(2);
//        ListenableFuture<? extends ProvisionedElement> rs = root.provision(new ErrorCollector(), ex);
//        ProvisionedElement proot = rs.get();
//
//        List<ProvisionedElement> leaves = Trees.leaves(proot);
//        assertThat(leaves.size(), equalTo(2));
//
//        assertThat(leaves, containsInstanceOf(ProvisionedServer.class));
//        assertThat(leaves, containsInstanceOf(ProvisionedError.class));
//        ex.shutdown();
//    }
}
