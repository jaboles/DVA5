package jb.common;

import java.util.Iterator;

public class Range implements Iterable<Integer> {
    private int min;
    private int count;

    public Range(int min, int count) {
        this.min = min;
        this.count = count;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int cur = min;
            private int count = Range.this.count;
            public boolean hasNext() {
                return count != 0;
            }

            public Integer next() {
                count--;
                return cur++; // first return the cur, then increase it.
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

