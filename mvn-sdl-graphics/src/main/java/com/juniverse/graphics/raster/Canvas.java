package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Rect;
import com.juniverse.graphics.style.RasterOperation;

/**
 * Drawing surface with coordinates relative to the active viewport.
 * <p>
 * It is the abstraction that all the high-level painters depend on
 * (dependency inversion principle): they know nothing about pages, viewports or
 * clipping, they only plot and read pixels.
 */
public interface Canvas {

    /** Value returned by {@link #read} for coordinates outside the drawable area. */
    int OUTSIDE = 0x00000000;

    /** Width of the drawing area (viewport). */
    int width();

    /** Height of the drawing area (viewport). */
    int height();

    /**
     * Rectangle (in viewport-relative coordinates) that contains all the
     * drawable points. If the viewport does not clip, it covers the whole page.
     */
    Rect drawableBounds();

    /** Tells whether a viewport-relative point can be drawn. */
    boolean isDrawable(int x, int y);

    /** Draws an ARGB pixel combining it with the given operation. */
    void plot(int x, int y, int argb, RasterOperation operation);

    /** Reads an ARGB pixel, or {@link #OUTSIDE} if the coordinate is not drawable. */
    int read(int x, int y);
}
