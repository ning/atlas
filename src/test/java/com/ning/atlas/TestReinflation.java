package com.ning.atlas;

public class TestReinflation
{
//    @Test
//    public void testFoo() throws Exception
//    {
//        JRubyTemplateParser p = new JRubyTemplateParser();
//        Environment e = p.parseEnvironment(new File("src/test/ruby/test_reinflation_env.rb"));
//        Template t = p.parseSystem(new File("src/test/ruby/test_reinflation_sys.rb"));
//
//        BoundTemplate bt = t.normalize(e);
//        ProvisionedElement pt = bt.provision(new ErrorCollector(),sameThreadExecutor()).get();
//        InitializedTemplate it = pt.initialize(new ErrorCollector(), sameThreadExecutor()).get();
//        InstalledElement inst = it.install(new ErrorCollector(), sameThreadExecutor()).get();
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
//        String json = mapper.writeValueAsString(inst);
//
//
//        Base.DESERIALIZATION_HACK.set(e);
//
//        InstalledElement inst2 = mapper.readValue(json, InstalledElement.class);
//
//
//        String json2 = mapper.writeValueAsString(inst2);
//
//        assertThat(json2, equalTo(json));
//    }
}
