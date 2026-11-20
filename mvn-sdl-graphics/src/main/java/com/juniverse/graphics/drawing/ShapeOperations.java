package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;

import java.util.List;

/**
 * Pixel and outline operations (unfilled shapes).
 */
public interface ShapeOperations {

    /** Draws a pixel with the given color code. */
    void putPixel(int x, int y, int colorCode);

    /** Reads the color code of a pixel (0 if outside the drawable area). */
    int getPixel(int x, int y);

    /** Draws the outline of a rectangle with the current line style. */
    void rectangle(int left, int top, int right, int bottom);

    /** Draws a circle. */
    void circle(int x, int y, int radius);

    /** Draws a circular arc between two angles (in degrees). */
    void arc(int x, int y, int startAngle, int endAngle, int radius);

    /** Draws an elliptical arc between two angles (in degrees). */
    void ellipse(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY);

    /** Draws an open polyline with the given vertices. */
    void drawPoly(List<Point> points);
}
