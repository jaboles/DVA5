package jb.common;

public final class RangeFactory {
    public static Iterable<Integer> range(int a, int b) {
        return new Range(a, b);
    }
}