package com.juniverse.graphics.style;

import java.util.Arrays;

/**
 * Immutable 8x8-pixel fill mask.
 * <p>
 * Each row is a byte; the most significant bit corresponds to the left
 * column. A bit set to 1 is painted with the fill color and a bit set to 0 with
 * the background color, just as in BGI.
 */
public final class PatternMask {

    /** Mask size (8x8). */
    public static final int SIZE = 8;

    private final int[] rows;

    private PatternMask(int[] rows) {
        this.rows = rows;
    }

    /**
     * Creates a mask from 8 rows (only the lower 8 bits are used).
     */
    public static PatternMask of(int... rows) {
        if (rows.length != SIZE) {
            throw new IllegalArgumentException("A fill pattern requires " + SIZE + " rows");
        }
        return new PatternMask(Arrays.stream(rows).map(row -> row & 0xFF).toArray());
    }

    /** Tells whether the bit corresponding to pixel (x, y) is set. */
    public boolean isSet(int x, int y) {
        return (rows[y & 7] & (0x80 >> (x & 7))) != 0;
    }

    /** Copy of the rows (equivalent to {@code getfillpattern}). */
    public int[] rows() {
        return rows.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PatternMask mask && Arrays.equals(rows, mask.rows);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rows);
    }

    @Override
    public String toString() {
        return "PatternMask" + Arrays.toString(rows);
    }
}
