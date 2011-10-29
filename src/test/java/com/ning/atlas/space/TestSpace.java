package com.ning.atlas.space;

import com.google.common.io.Files;
import com.ning.atlas.base.Maybe;
import com.ning.atlas.spi.Identity;
import com.ning.atlas.spi.Space;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestSpace
{
    private Space space;
    private File  tempDir;

    @Before
    public void setUp() throws Exception
    {
        this.tempDir = Files.createTempDir();
        this.space = DiskBackedSpace.create(tempDir);

        // this.space = InMemorySpace.newInstance();
    }

    public void tearDown() throws Exception
    {
        Files.deleteRecursively(tempDir);
    }

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

        Map<String, String> all = space.getAllFor(id);
        assertThat(all.get(id.toExternalForm() + ":" + "name"), equalTo("Freddy"));
        assertThat(all.get(id.toExternalForm() + ":" + "age-of-pet-dog"), equalTo("7"));

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
}
