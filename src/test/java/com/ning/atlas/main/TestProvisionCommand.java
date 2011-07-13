package com.ning.atlas.main;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestProvisionCommand
{
    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        MainOptions opts = new MainOptions("-e", "src/test/ruby/ex1/static-tagged.rb",
                                           "-s", "src/test/ruby/ex1/static-tagged.rb",
                                           "provision");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = System.out;
        System.setOut(new PrintStream(buf));
        opts.getCommand().create(opts).run();
        System.out.flush();
        System.setOut(out);

        String json = new String(buf.toByteArray());

        Thing skife = new ObjectMapper().readValue(json, Thing.class);
        assertThat(skife.type, equalTo("skife"));
        assertThat(skife.children.size(), equalTo(2));


        Thing blog = skife.children.get(0);
        assertThat(blog.server.internal_address, equalTo("10.0.0.1"));

        Thing data = skife.children.get(1);
        assertThat(data.type, equalTo("data"));
        assertThat(data.children.size(), equalTo(3));


        final Thing m1 = Iterables.find(data.children, new Predicate<Thing>()
        {
            @Override
            public boolean apply(@Nullable Thing input)
            {
                return input.type.equals("memcached");
            }
        });

        assertThat(m1.type, equalTo("memcached"));
        assertThat(m1.server.internal_address, equalTo("10.0.1.1"));

        Thing m2 = Iterables.find(data.children, new Predicate<Thing>()
        {
            @Override
            public boolean apply(@Nullable Thing input)
            {
                return input.type.equals("memcached") && input != m1;
            }
        });
        assertThat(m2.type, equalTo("memcached"));
        assertThat(m2.server.internal_address, equalTo("10.0.1.2"));

        Thing db = Iterables.find(data.children, new Predicate<Thing>()
        {
            @Override
            public boolean apply(@Nullable Thing input)
            {
                return input.type.equals("db");
            }
        });
        assertThat(db.type, equalTo("db"));
        assertThat(db.server.internal_address, equalTo("10.0.0.2"));

    }

    public static class S
    {
        public String internal_address;
        public String external_address;
    }


    @JsonIgnoreProperties("environment")
    public static class Thing
    {
        public String type;
        public String name;
        public List<Thing> children;
        public Map<String, Object> my;
        public S server;


        @Override
        public String toString()
        {
            return "Thing{" +
                   "name='" + name + '\'' +
                   ", type=" + type +
                   ", children=" + children +
                   '}';
        }
    }
}
