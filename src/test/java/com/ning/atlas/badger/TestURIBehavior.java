package com.ning.atlas.badger;

import com.ning.atlas.Uri;
import org.junit.Test;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestURIBehavior
{
    @Test
    public void testScheme() throws Exception
    {
        Uri uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getScheme(), equalTo("provisioner"));
    }

    @Test
    public void testJustScheme() throws Exception
    {
        Uri uri = Uri.valueOf("waffle");
        assertThat(uri.getScheme(), equalTo("waffle"));
        assertThat(uri.getFragment(), equalTo(""));
    }

    @Test
    public void testFragment() throws Exception
    {
        Uri uri = Uri.valueOf("provisioner:galaxy:v2");
        assertThat(uri.getFragment(), equalTo("galaxy:v2"));
    }

}
