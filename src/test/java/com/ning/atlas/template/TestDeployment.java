package com.ning.atlas.template;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
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

        SystemManifest d = SystemManifest.build(env, root);
        assertFalse(d.getInstances().isEmpty());
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


        SystemManifest d = SystemManifest.build(env, sys);

        assertEquals(5, d.getInstances().size());
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

        SystemManifest d = SystemManifest.build(env, sys);

        assertEquals(3, d.getInstances().size());
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

        SystemManifest d = SystemManifest.build(env, root);

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

        SystemManifest d = SystemManifest.build(env, root);

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

        SystemManifest d = SystemManifest.build(env, root);

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

        SystemManifest d = SystemManifest.build(env, root);

        assertEquals(30, d.getInstances().size());
    }


    @Test
    public void testNameExpansion() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig();
        SystemTemplate sys = new SystemTemplate("test");
        ServerTemplate named = new NamedCore();

        sys.addChild(named, 5);

        SystemManifest d = SystemManifest.build(env, sys);

        assertEquals(5, d.getInstances().size());
    }


    /* support classes */


    class NamedCore extends ServerTemplate
    {
        NamedCore()
        {
            super("named-<id>");
        }
    }

    class AppCore extends ServerTemplate
    {
        public AppCore()
        {
            super("appcore");
        }
    }

    class Memcache extends ServerTemplate
    {

        public Memcache()
        {
            super("memcache");
        }
    }

    class JobCore extends ServerTemplate
    {

        public JobCore()
        {
            super("jobc");
        }
    }

    class Resolver extends ServerTemplate
    {

        public Resolver()
        {
            super("resolver");
        }
    }
}
