package com.ning.atlas;

import com.ning.atlas.upgrade.UpgradePlan;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static com.ning.atlas.Jsonificator.jsonify;
import static com.ning.atlas.Jsonificator.reify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

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

    @Test
    public void testFoo() throws Exception
    {
        assumeThat(1+1, equalTo(3));
        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_updates_env.rb"));
        InstalledElement one = p.parseSystem(new File("src/test/ruby/test_updates_sys_1.rb"))
                                 .normalize(e)
                                 .provision(sameThreadExecutor()).get()
                                 .initialize(sameThreadExecutor()).get()
                                 .install(sameThreadExecutor()).get();

        assertThat(jsonify(reify(e, jsonify(one))), equalTo(jsonify(one)));
        InstalledElement reified = reify(e, jsonify(one));

        BoundTemplate two = p.parseSystem(new File("src/test/ruby/test_updates_sys_2.rb"))
                             .normalize(e);

        UpgradePlan plan = two.upgradeFrom(reified);

//                                 .provision(sameThreadExecutor()).get()
//                                 .initialize(sameThreadExecutor()).get();

    }
}
