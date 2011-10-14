package com.ning.atlas;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertThat;

public class TestUpdates
{
    @Before
    public void setUp() throws Exception
    {

    }

    @After
    public void tearDown() throws Exception
    {

    }

//    @Test
//    public void testFoo() throws Exception
//    {
//        assumeThat(1+1, equalTo(3));
//        JRubyTemplateParser p = new JRubyTemplateParser();
//        Environment e = p.parseEnvironment(new File("src/test/ruby/test_updates_env.rb"));
//        InstalledElement one = p.parseSystem(new File("src/test/ruby/test_updates_sys_1.rb"))
//                                 .normalize(e)
//                                 .provision(new ErrorCollector(),sameThreadExecutor()).get()
//                                 .initialize(new ErrorCollector(), sameThreadExecutor()).get()
//                                 .install(new ErrorCollector(), sameThreadExecutor()).get();
//
//        assertThat(jsonify(reify(e, jsonify(one))), equalTo(jsonify(one)));
//        InstalledElement reified = reify(e, jsonify(one));
//
//        BoundTemplate two = p.parseSystem(new File("src/test/ruby/test_updates_sys_2.rb"))
//                             .normalize(e);
//
//        List<Change> plan = two.upgradeFrom(reified);
//
//                                 .provision(sameThreadExecutor()).get()
//                                 .initialize(sameThreadExecutor()).get();
//
//    }
}
