package com.juniverse.graphics.dto;

/**
 * Information about the last arc drawn (equivalent to {@code arccoordstype}).
 *
 * @param x      horizontal center of the arc
 * @param y      vertical center of the arc
 * @param xStart x coordinate of the start point
 * @param yStart y coordinate of the start point
 * @param xEnd   x coordinate of the end point
 * @param yEnd   y coordinate of the end point
 */
public record ArcCoordinates(int x, int y, int xStart, int yStart, int xEnd, int yEnd) {

    /** Initial value before any arc is drawn. */
    public static final ArcCoordinates EMPTY = new ArcCoordinates(0, 0, 0, 0, 0, 0);
}
