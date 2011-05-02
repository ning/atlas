package com.ning.atlas.template;

import com.ning.atlas.template2.Environment;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class TestDeployment
{

    private Environment environment = new Environment("env");


    @Test
    public void testSomethingUseful() throws Exception
    {
        ConfigurableSystemTemplate root = new ConfigurableSystemTemplate("ning");
        root.addChild(new Resolver(), 5);

        ConfigurableSystemTemplate aclu = new ConfigurableSystemTemplate("aclu<id>");
        aclu.addChild(new AppCore(), 1);
        aclu.addChild(new Memcache(), 1);

        root.addChild(aclu, 1);

        root.addChild(new JobCore(), 1);

        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.addDeployVar("aclu-names", Arrays.<String>asList("00", "01"));

        NormalizedTemplate d = NormalizedTemplate.build(env, root);
        assertFalse(d.getInstances().isEmpty());
    }

    @Test
    public void testSimpleSystem() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");


        ConfigurableSystemTemplate sys = new ConfigurableSystemTemplate("test");
        ConfigurableServerTemplate appCore = new AppCore();

        sys.addChild(appCore, 5); // 5 appcores


        NormalizedTemplate d = NormalizedTemplate.build(env, sys);

        assertEquals(5, d.getInstances().size());
    }


    @Test
    public void testEnvOverrideOfCardinality() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/appcore", 3);  // 3 appcore sin this env

        ConfigurableSystemTemplate sys = new ConfigurableSystemTemplate("test");
        ConfigurableServerTemplate appCore = new AppCore();

        sys.addChild(appCore, 5); // 5 appcores

        NormalizedTemplate d = NormalizedTemplate.build(env, sys);

        assertEquals(3, d.getInstances().size());
    }

    @Test
    public void testCardinalityOfSystems() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        DeployTemplate root = new ConfigurableSystemTemplate("test");
        DeployTemplate aclu = root.addChild(new ConfigurableSystemTemplate("aclu"), 2);
        aclu.addChild(new AppCore(), 5); // 5 appcores

        NormalizedTemplate d = NormalizedTemplate.build(env, root);

        assertEquals(10, d.getInstances().size());

    }

    @Test
    public void testEnvOverrideOfCardinalityOfSystems() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/aclu", 5);

        DeployTemplate root = new ConfigurableSystemTemplate("test");
        DeployTemplate aclu = root.addChild(new ConfigurableSystemTemplate("aclu"), 2);
        aclu.addChild(new AppCore(), 5);

        NormalizedTemplate d = NormalizedTemplate.build(env, root);

        assertEquals(25, d.getInstances().size());
    }

    @Test
    public void testNestedSystems() throws Exception
    {

        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

//        env.overrideCardinality("/test/aclu", 5);

        DeployTemplate root = new ConfigurableSystemTemplate("test");
        DeployTemplate aclu = root.addChild(new ConfigurableSystemTemplate("aclu"), 2);
        DeployTemplate aclu2 = aclu.addChild(new ConfigurableSystemTemplate("aclu2"), 2);
        aclu2.addChild(new AppCore(), 5);

        NormalizedTemplate d = NormalizedTemplate.build(env, root);

        assertEquals(20, d.getInstances().size());
    }


    @Test
    public void testNestedSystemsWithEnvOverride() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        env.addConfigVar("xn.db.url", "jdbc:kyoto:/tmp/foo.kch");
        env.addConfigVar("xn.db.user", "joe");
        env.addConfigVar("xn.db.password", "security-is-fun");

        env.overrideCardinality("/test/aclu/aclu2", 3);

        DeployTemplate root = new ConfigurableSystemTemplate("test");
        DeployTemplate aclu = root.addChild(new ConfigurableSystemTemplate("aclu"), 2);
        DeployTemplate aclu2 = aclu.addChild(new ConfigurableSystemTemplate("aclu2"), 2);
        aclu2.addChild(new AppCore(), 5);

        NormalizedTemplate d = NormalizedTemplate.build(env, root);

        assertEquals(30, d.getInstances().size());
    }


    @Test
    public void testNameExpansion() throws Exception
    {
        EnvironmentConfig env = new EnvironmentConfig(environment);
        ConfigurableSystemTemplate sys = new ConfigurableSystemTemplate("test");
        ConfigurableServerTemplate named = new NamedCore();

        sys.addChild(named, 5);

        NormalizedTemplate d = NormalizedTemplate.build(env, sys);

        assertEquals(5, d.getInstances().size());
    }


    /* support classes */


    class NamedCore extends ConfigurableServerTemplate
    {
        NamedCore()
        {
            super("named-<id>");
        }
    }

    class AppCore extends ConfigurableServerTemplate
    {
        public AppCore()
        {
            super("appcore");
        }
    }

    class Memcache extends ConfigurableServerTemplate
    {

        public Memcache()
        {
            super("memcache");
        }
    }

    class JobCore extends ConfigurableServerTemplate
    {

        public JobCore()
        {
            super("jobc");
        }
    }

    class Resolver extends ConfigurableServerTemplate
    {

        public Resolver()
        {
            super("resolver");
        }
    }
}
