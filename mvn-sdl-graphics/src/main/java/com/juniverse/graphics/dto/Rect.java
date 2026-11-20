package com.juniverse.graphics.dto;

/**
 * Immutable rectangle defined by its (inclusive) edges.
 */
public record Rect(int left, int top, int right, int bottom) {

    /** Returns the rectangle with its corners ordered (left &lt;= right, top &lt;= bottom). */
    public Rect normalized() {
        return new Rect(Math.min(left, right), Math.min(top, bottom),
                Math.max(left, right), Math.max(top, bottom));
    }

    /** Width in pixels (inclusive edges). */
    public int width() {
        return Math.abs(right - left) + 1;
    }

    /** Height in pixels (inclusive edges). */
    public int height() {
        return Math.abs(bottom - top) + 1;
    }

    /** Tells whether the point is inside the rectangle. */
    public boolean contains(int x, int y) {
        Rect r = normalized();
        return x >= r.left && x <= r.right && y >= r.top && y <= r.bottom;
    }
}
