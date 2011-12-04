package com.ning.atlas;

import com.ning.atlas.spi.Uri;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestStringTemplateBehavior
{
    @Test
    public void testMapAccess() throws Exception
    {
        ST st = new ST("hello {base.params.q}", '{', '}');
        st.add("base", Uri.valueOf("scheme:fragment?q=v"));
        assertThat(st.render(), equalTo("hello v"));
    }
}
