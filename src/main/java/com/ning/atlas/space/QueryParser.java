package com.ning.atlas.space;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class QueryParser
{
    /*
     /#/foo.#:key
     */
    public SpaceQuery parse(String expression)
    {
        Iterator<String> blah = Splitter.on(':').split(expression).iterator();
        String id_query = blah.next();
        String key = blah.next();

        Iterator<String> parts = Splitter.on('/').split(id_query).iterator();
        parts.next(); // skip root

        final List<Predicate<Pair<String, String>>> tests = Lists.newArrayList();

        while (parts.hasNext()) {
            String part = parts.next();
            if ("*".equals(part)) {
                // match anything
                tests.add(Predicates.<Pair<String, String>>alwaysTrue());
            }
            else {
                Iterator<String> s = Splitter.on('.').split(part).iterator();
                String type = s.next();

                final Predicate<String> type_test;
                if ("*".equals(type)) {
                    // any type
                    type_test = Predicates.alwaysTrue();
                }
                else {
                    type_test = Predicates.equalTo(type);
                }

                String name = s.next();
                final Predicate<String> name_test;
                if ("*".equals(name)) {
                    name_test = Predicates.alwaysTrue();
                }
                else {
                    name_test = Predicates.equalTo(name);
                }
                tests.add(new Predicate<Pair<String, String>>()
                {
                    @Override
                    public boolean apply(Pair<String, String> input)
                    {
                        return type_test.apply(input.getKey()) && name_test.apply(input.getValue());
                    }
                });

            }
        }

        return new SpaceQuery(tests, key);
    }
}
