package com.ning.atlas.space;

import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestSpace
{
    @Test
    public void testFoo() throws Exception
    {
        Space space = InMemorySpace.newInstance();
        Identity id = Identity.root().createChild("test", "0");

        Thing t = new Thing();
        t.setName("Freddy");
        t.setAgeOfPetDog(14);

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.RequireAll);

        assertThat(t, equalTo(t2.getValue()));
    }

    @Test
    public void testOtherObjectsSameProperties() throws Exception
    {
        Space space = InMemorySpace.newInstance();
        Identity id = Identity.root().createChild("test", "0");

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
        Space space = InMemorySpace.newInstance();
        Identity id = Identity.root().createChild("test", "0");

        NameOnly t = new NameOnly();
        t.setName("Freddy");

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.RequireAll);

        assertThat(t2.isKnown(), equalTo(false));
    }

    @Test
    public void testNullValueWhenMissing() throws Exception
    {
        Space space = InMemorySpace.newInstance();
        Identity id = Identity.root().createChild("test", "0");

        NameOnly t = new NameOnly();
        t.setName("Freddy");

        space.store(id, t);

        Maybe<Thing> t2 = space.get(id, Thing.class, Missing.NullProperty);

        assertThat(t2.isKnown(), equalTo(true));
        assertThat(t2.getValue().getAgeOfPetDog(), nullValue());

    }

    public static class NameOnly {
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
        private String name;
        private Integer ageOfPetDog;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Integer getAgeOfPetDog() {
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
}
