package com.ning.atlas.space;

import com.google.common.collect.Lists;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Maybe;
import com.ning.atlas.spi.space.Missing;
import com.ning.atlas.spi.space.Space;
import com.ning.atlas.spi.space.SpaceKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class BaseSpaceTest
{
    private Space space;

    @Before
    public final void setUp() throws Exception
    {
        localSetUp();
        this.space = createSpace();
    }

    protected abstract Space createSpace() throws IOException;
    protected abstract void destroySpace(Space space) throws IOException;

    protected void localSetUp() {}

    @After
    public void tearDown() throws Exception
    {
        localTearDown();
    }

    protected void localTearDown() throws Exception {}

    @Test
    public void testFoo() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0").createChild("waffle", "9");

        Thing t = new Thing();
        t.setName("Freddy");
        t.setAgeOfPetDog(14);

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.RequireAll);

        assertThat(t, equalTo(t2.getValue()));
    }

    @Test
    public void testReadAll() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0");
        space.store(id, "hello", "world");
        space.store(id, "favorite", "color");
        space.store(id, "flavor", "strawberry");

        Map<SpaceKey, String> rs = space.getAllFor(id);
        assertThat(rs.size(), equalTo(3));
        assertThat(rs.get(SpaceKey.from(id, "hello")), equalTo("world"));
        assertThat(rs.get(SpaceKey.from(id, "favorite")), equalTo("color"));
        assertThat(rs.get(SpaceKey.from(id, "flavor")), equalTo("strawberry"));
    }

    @Test
    public void testReadAllWithChildIdentitis() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0");
        space.store(id, "hello", "world");
        space.store(id.createChild("color", "wheel"), "favorite", "color");
        space.store(id, "flavor", "strawberry");

        Map<SpaceKey, String> rs = space.getAllFor(id);
        assertThat(rs.size(), equalTo(3));
        assertThat(rs.get(SpaceKey.from(id, "hello")), equalTo("world"));
        assertThat(rs.get(SpaceKey.from(id.createChild("color", "wheel"), "favorite")), equalTo("color"));
        assertThat(rs.get(SpaceKey.from(id, "flavor")), equalTo("strawberry"));
    }

    @Test
    public void testOtherObjectsSameProperties() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0").createChild("waffle", "9");

        Thing t = new Thing();
        t.setName("Freddy");
        t.setAgeOfPetDog(14);

        space.store(id, t);

        NameOnly no = space.get(id, NameOnly.class, Missing.RequireAll).getValue();
        assertThat(no.getName(), equalTo(t.getName()));
    }

    @Test
    public void testRequireAll() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0").createChild("waffle", "9");

        NameOnly t = new NameOnly();
        t.setName("Freddy");

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.RequireAll);

        assertThat(t2.isKnown(), equalTo(false));
    }

    @Test
    public void testNullValueWhenMissing() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0").createChild("waffle", "9");

        NameOnly t = new NameOnly();
        t.setName("Freddy");

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.NullProperty);

        assertThat(t2.isKnown(), equalTo(true));
        assertThat(t2.getValue().getAgeOfPetDog(), nullValue());

    }

    @Test
    public void testListAttributes() throws Exception
    {
        Identity id = Identity.root().createChild("test", "0").createChild("waffle", "9");

        Thing t = new Thing();
        t.setName("Freddy");
        t.setAgeOfPetDog(7);

        space.store(id, t);

        Map<SpaceKey, String> all = space.getAllFor(id);
        assertThat(all.get(SpaceKey.from(id, "name")), equalTo("Freddy"));
        assertThat(all.get(SpaceKey.from(id, "age-of-pet-dog")), equalTo("7"));

    }

    @Test
    public void testComplexObjectBehavior() throws Exception
    {
        Identity id = Identity.root().createChild("waffle", "ketchup");
        ComplexThing ct = new ComplexThing();
        ct.setNumbers(Lists.<Integer>newArrayList(1,2,3,4,5));
        ct.setName("fred");
        space.store(id, ct);

        ComplexThing it = space.get(id, ComplexThing.class)
                               .otherwise(new IllegalStateException("unabel to find stuff"));

        assertThat(EqualsBuilder.reflectionEquals(ct, it), equalTo(true));
    }

    @Test
    public void testComplexObjectBehavior2() throws Exception
    {
        Identity id = Identity.root().createChild("waffle", "ketchup");
        ComplexThing ct = new ComplexThing();
        ct.setNumbers(Lists.<Integer>newArrayList(1,2,3,4,5));
        ct.setName("fred");
        space.store(id, ct);

        String nums = space.get(id, "numbers").otherwise(new IllegalStateException("fail"));
        String name = space.get(id, "name").otherwise(new IllegalStateException("fail"));

        assertThat((List<Integer>)new ObjectMapper().readValue(nums, List.class), equalTo(ct.getNumbers()));
        assertThat(name, equalTo("fred"));
    }

    @Test
    public void testComplexObjectBehaviorWithWrappers() throws Exception
    {
        Identity id = Identity.root().createChild("waffle", "ketchup");
        ThingWithWrapper ct = new ThingWithWrapper();
        ct.setNumber(7);
        space.store(id, ct);

        String number = space.get(id, "number").otherwise(new IllegalStateException("fail"));

        assertThat(number, equalTo("7"));
    }

    public static class NameOnly
    {
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    public static class Thing
    {
        private String  name;
        private Integer ageOfPetDog;

        public void setName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public Integer getAgeOfPetDog()
        {
            return ageOfPetDog;
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        public void setAgeOfPetDog(Integer ageOfPetDog)
        {
            this.ageOfPetDog = ageOfPetDog;
        }
    }

    public static class ThingWithWrapper
    {
        private Integer number;

        public Integer getNumber()
        {
            return number;
        }

        public void setNumber(Integer number)
        {
            this.number = number;
        }
    }

    public static class ComplexThing
    {
        private List<Integer> numbers;
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<Integer> getNumbers()
        {
            return numbers;
        }

        public void setNumbers(List<Integer> numbers)
        {
            this.numbers = numbers;
        }
    }
}
