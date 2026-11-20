package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;

import java.util.List;

/**
 * Operations for shapes filled with the current fill pattern and color.
 */
public interface FillOperations {

    /** Fills a rectangle without drawing its border. */
    void bar(int left, int top, int right, int bottom);

    /** Draws a filled bar with a three-dimensional outline. */
    void bar3d(int left, int top, int right, int bottom, int depth, boolean drawTop);

    /** Draws and fills a complete ellipse. */
    void fillEllipse(int x, int y, int radiusX, int radiusY);

    /** Draws and fills a closed polygon. */
    void fillPoly(List<Point> points);

    /** Draws and fills a circular sector ("pie slice"). */
    void pieSlice(int x, int y, int startAngle, int endAngle, int radius);

    /** Draws and fills an elliptical sector. */
    void sector(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY);

    /** Fills the region containing (x, y) delimited by the border color. */
    void floodFill(int x, int y, int borderColorCode);
}
