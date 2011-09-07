package com.ning.atlas;

import com.ning.atlas.errors.ErrorCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import java.io.File;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestReinflation
{
    @Test
    public void testFoo() throws Exception
    {

        JRubyTemplateParser p = new JRubyTemplateParser();
        Environment e = p.parseEnvironment(new File("src/test/ruby/test_reinflation_env.rb"));
        Template t = p.parseSystem(new File("src/test/ruby/test_reinflation_sys.rb"));

        BoundTemplate bt = t.normalize(e);
        ProvisionedElement pt = bt.provision(new ErrorCollector(),sameThreadExecutor()).get();
        InitializedTemplate it = pt.initialize(sameThreadExecutor()).get();
        InstalledElement inst = it.install(sameThreadExecutor()).get();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        String json = mapper.writeValueAsString(inst);


        Base.DESERIALIZATION_HACK.set(e);

        InstalledElement inst2 = mapper.readValue(json, InstalledElement.class);


        String json2 = mapper.writeValueAsString(inst2);

        assertThat(json2, equalTo(json));
    }
}
