import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.InputStreamReader;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestCruft
{
    @Test
    public void testFoo() throws Exception
    {
        URI uri = URI.create("file:///Users/brianm/src/atlas/test.txt?user=atlas&pass=atlas");
        String content = CharStreams.toString(new InputStreamReader(uri.toURL().openStream()));
        assertThat(content, equalTo("hello world"));
    }
}
