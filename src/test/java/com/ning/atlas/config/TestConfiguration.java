package com.ning.atlas.config;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestConfiguration
{
    private File recorded;

    private File atlas;

    @Before
    public void setUp() throws Exception
    {
        this.atlas = File.createTempFile("atlas", ".conf");
        this.recorded = File.createTempFile("recorded", ".conf");
    }

    @After
    public void tearDown() throws Exception
    {
        this.atlas.delete();
        this.recorded.delete();
    }

    @Test
    public void testReadFromRecorded() throws Exception
    {
        Files.write("name=Brian", recorded, Charsets.UTF_8);
        AtlasConfiguration config = new AtlasConfiguration(atlas, recorded);
        assertThat(config.lookup("name"), equalTo(Optional.of("Brian")));
    }

    @Test
    public void testReadFromAtlas() throws Exception
    {
        Files.write("name=Brian", atlas, Charsets.UTF_8);
        AtlasConfiguration config = new AtlasConfiguration(atlas, recorded);
        assertThat(config.lookup("name"), equalTo(Optional.of("Brian")));
    }

    @Test
    public void testAtlasOverridesRecorded() throws Exception
    {
        Files.write("name=Brian", atlas, Charsets.UTF_8);
        Files.write("name=Eric", recorded, Charsets.UTF_8);
        AtlasConfiguration config = new AtlasConfiguration(atlas, recorded);
        assertThat(config.lookup("name"), equalTo(Optional.of("Brian")));
    }

    @Test
    public void testRecordActuallyWritesToDisk() throws Exception
    {
        AtlasConfiguration config = new AtlasConfiguration(atlas, recorded);
        config.record("name", "Brian");
        assertThat(config.lookup("name"), equalTo(Optional.of("Brian")));
        List<String> lines = Files.readLines(recorded, Charsets.UTF_8);
        assertThat(find(lines, Predicates.equalTo("name=Brian")), notNullValue());
    }

}
