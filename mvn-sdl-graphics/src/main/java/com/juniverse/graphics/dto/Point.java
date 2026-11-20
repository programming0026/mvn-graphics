package com.juniverse.graphics.dto;

/**
 * Immutable point in integer screen coordinates.
 *
 * @param x horizontal coordinate (grows to the right)
 * @param y vertical coordinate (grows downward)
 */
public record Point(int x, int y) {

    /** Point that represents "no click" (equivalent to {@code NO_CLICK}). */
    public static final Point NONE = new Point(-1, -1);

    /** Returns a new, shifted point. */
    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }
}
