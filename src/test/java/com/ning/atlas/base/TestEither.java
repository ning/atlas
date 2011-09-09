package com.ning.atlas.base;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestEither
{
    @Test
    public void testLeft() throws Exception
    {
        Either<String, Integer> v = Either.success("hello world");
        switch(v.getSide()) {
            case Success:
                assertThat(v.getSuccess(), equalTo("hello world"));
                break;
            case Failure:
                fail("should have taken left branch");
                break;
        }
    }

    @Test
    public void testRight() throws Exception
    {
        Either<String, Integer> v = Either.failure(7);
        switch(v.getSide()) {
            case Success:
                fail("should have taken right branch");
                break;
            case Failure:
                assertThat(v.getFailure(), equalTo(7));
                break;
        }
    }
}
