package com.ning.atlas.space;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.space.Space;
import org.apache.commons.lang3.tuple.Pair;
import sun.java2d.pipe.OutlineTextRenderer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpaceQuery
{
    private final List<Predicate<Pair<String, String>>> tests;
    private final String key;

    public SpaceQuery(List<Predicate<Pair<String, String>>> tests, String key)
    {
        this.tests = tests;
        this.key = key;
    }

    public Set<String> query(Space space) {
        Set<String> rs = Sets.newLinkedHashSet();
        OUTER:
        for (Identity identity : space.findAllIdentities()) {
            Iterator<String> parts = Splitter.on('/').split(identity.toExternalForm()).iterator();
            parts.next(); // skip root
            Iterator<Predicate<Pair<String, String>>> test_itty = tests.iterator();

            while (parts.hasNext() && test_itty.hasNext()) {
                String part_together = parts.next();
                Predicate<Pair<String, String>> test = test_itty.next();
                Iterator<String> pitty = Splitter.on('.').split(part_together).iterator();
                Pair<String, String> part = Pair.of(pitty.next(), pitty.next());
                if (!test.apply(part)) {
                    continue OUTER;
                }
            }

            if (!parts.hasNext() && !test_itty.hasNext()) {
                Maybe<String> maybe = space.get(identity, key);
                if (maybe.isKnown()) {
                    rs.add(maybe.getValue());
                }
            }
        }
        return rs;
    }
}
