package com.ning.atlas.base;

public class Either<L, R>
{
    private final Side side;

    private Either(Side side)
    {
        this.side = side;
    }

    public static <L, R> Either<L, R> success(L leftValue)
    {
        return new Left<L, R>(leftValue);
    }

    public static <L, R> Either<L, R> failure(R rightValue)
    {
        return new Right<L, R>(rightValue);
    }

    public Side getSide()
    {
        return side;
    }

    public R getFailure() {
        throw new IllegalStateException("There is no success value");
    }

    public L getSuccess() {
        throw new IllegalStateException("There is no failure value");
    }

    private static class Right<L, R> extends Either<L, R>
    {
        private R rightValue;

        private Right(R rightValue)
        {
            super(Side.Failure);
            this.rightValue = rightValue;
        }

        @Override
        public R getFailure()
        {
            return rightValue;
        }
    }

    private static class Left<L, R> extends Either<L, R>
    {
        private final L leftValue;

        private Left(L leftValue)
        {
            super(Side.Success);
            this.leftValue = leftValue;
        }

        @Override
        public L getSuccess()
        {
            return this.leftValue;
        }
    }

    public enum Side
    {
        Success, Failure
    }
}
