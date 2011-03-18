package com.ning.atlas.template;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class TestDeployment
{
    @Test
    public void testSomethingUseful() throws Exception
    {
        SystemTemplate root = new SystemTemplate("ning");
        root.addChild(new Resolver(), 5);

        SystemTemplate aclu = new SystemTemplate("aclu<id>");
        aclu.addChild(new AppCore(), 1);
        aclu.addChild(new Memcache(), 1);

        root.addChild(aclu, 1);

        root.addChild(new JobCore(), 1);

        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.addDeployVar("aclu-names", Arrays.<String>asList("00", "01"));

        Deployment d = Deployment.build(env, root);
        assertFalse(d.getInstances().isEmpty());
    }


    @Test
    public void testDeploySingleServiceWithoutRequiredProperty() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        // env.addConfigVar("xn.db.user", "joe"); // missing required prop
        env.addConfigVar("xn.db.password", "security-is-fun");

        DeployTemplate dt = new AppCore();


        Deployment d = Deployment.build(env, dt);

        List<Instance> faults = d.validate();
        assertEquals(1, faults.size());
    }

    @Test
    public void testDeploySingleServiceWithAllRequiredProperties() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        DeployTemplate dt = new AppCore();

        Deployment d = Deployment.build(env, dt);

        List<Instance> faults = d.validate();
        assertEquals(0, faults.size());
    }


    @Test
    public void testSimpleSystem() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");


        SystemTemplate sys = new SystemTemplate("test");
        ServerTemplate appCore = new AppCore();

        sys.addChild(appCore, 5); // 5 appcores


        Deployment d = Deployment.build(env, sys);

        assertEquals(5, d.getInstances().size());

        List<Instance> faults = d.validate();
        assertEquals(0, faults.size());


    }


    @Test
    public void testEnvOverrideOfCardinality() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/appcore", 3);  // 3 appcore sin this env

        SystemTemplate sys = new SystemTemplate("test");
        ServerTemplate appCore = new AppCore();

        sys.addChild(appCore, 5); // 5 appcores

        Deployment d = Deployment.build(env, sys);

        assertEquals(3, d.getInstances().size());

        List<Instance> faults = d.validate();
        assertEquals(0, faults.size());
    }

    @Test
    public void testCardinalityOfSystems() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        DeployTemplate root = new SystemTemplate("test");
        DeployTemplate aclu = root.addChild(new SystemTemplate("aclu"), 2);
        aclu.addChild(new AppCore(), 5); // 5 appcores

        Deployment d = Deployment.build(env, root);

        assertEquals(10, d.getInstances().size());

    }

    @Test
    public void testEnvOverrideOfCardinalityOfSystems() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/aclu", 5);

        DeployTemplate root = new SystemTemplate("test");
        DeployTemplate aclu = root.addChild(new SystemTemplate("aclu"), 2);
        aclu.addChild(new AppCore(), 5);

        Deployment d = Deployment.build(env, root);

        assertEquals(25, d.getInstances().size());
    }

    @Test
    public void testNestedSystems() throws Exception
    {

        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

//        env.overrideCardinality("/test/aclu", 5);

        DeployTemplate root = new SystemTemplate("test");
        DeployTemplate aclu = root.addChild(new SystemTemplate("aclu"), 2);
        DeployTemplate aclu2 = aclu.addChild(new SystemTemplate("aclu2"), 2);
        aclu2.addChild(new AppCore(), 5);

        Deployment d = Deployment.build(env, root);

        assertEquals(20, d.getInstances().size());
    }


    @Test
    public void testNestedSystemsWithEnvOverride() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/aclu/aclu2", 3);

        DeployTemplate root = new SystemTemplate("test");
        DeployTemplate aclu = root.addChild(new SystemTemplate("aclu"), 2);
        DeployTemplate aclu2 = aclu.addChild(new SystemTemplate("aclu2"), 2);
        aclu2.addChild(new AppCore(), 5);

        Deployment d = Deployment.build(env, root);

        assertEquals(30, d.getInstances().size());
    }


    @Test
    public void testNameExpansion() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        SystemTemplate sys = new SystemTemplate("test");
        ServerTemplate named = new NamedCore();

        sys.addChild(named, 5);

        Deployment d = Deployment.build(env, sys);

        assertEquals(5, d.getInstances().size());

        List<Instance> faults = d.validate();
        assertEquals(0, faults.size());
    }


    /* support classes */


    class NamedCore extends ServerTemplate
    {
        NamedCore() {
            super("named-<id>", Collections.<String>emptyList());
        }
    }

    class AppCore extends ServerTemplate
    {
        public AppCore()
        {
            super("appcore", Collections.<String>emptyList());
            addRequiredProperties("xn.db.url",
                                  "xn.db.user",
                                  "xn.db.password");
        }
    }

    class Memcache extends ServerTemplate
    {

        public Memcache()
        {
            super("memcache", Collections.<String>emptyList());
        }
    }

    class JobCore extends ServerTemplate
    {

        public JobCore()
        {
            super("jobc", Collections.<String>emptyList());
        }
    }

    class Resolver extends ServerTemplate
    {

        public Resolver()
        {
            super("resolver", Collections.<String>emptyList());
            addRequiredProperties("xn.external.base-domain",
                                  "xn.db.url",
                                  "xn.db.user",
                                  "xn.db.password");
        }
    }
}
