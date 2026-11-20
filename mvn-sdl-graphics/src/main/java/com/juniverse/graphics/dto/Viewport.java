package com.juniverse.graphics.dto;

/**
 * Active drawing area (equivalent to {@code viewporttype}).
 * <p>
 * All drawing coordinates are relative to the top-left
 * corner of the viewport. If {@code clip} is true, whatever falls outside
 * the viewport is not drawn.
 */
public record Viewport(int left, int top, int right, int bottom, boolean clip) {

    /** Creates a viewport, validating that the coordinates are ordered. */
    public Viewport {
        if (right < left || bottom < top) {
            throw new IllegalArgumentException("Invalid viewport: (" + left + "," + top
                    + ")-(" + right + "," + bottom + ")");
        }
    }

    /** Viewport that covers a whole window without clipping. */
    public static Viewport fullScreen(int width, int height) {
        return new Viewport(0, 0, width - 1, height - 1, false);
    }

    /** Width of the viewport in pixels. */
    public int width() {
        return right - left + 1;
    }

    /** Height of the viewport in pixels. */
    public int height() {
        return bottom - top + 1;
    }

    /** Tells whether an absolute screen point falls inside the viewport. */
    public boolean containsAbsolute(int x, int y) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
